/*
  Copyright 2018 Ryos.io.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package io.ryos.rhino.sdk;

import static io.ryos.rhino.sdk.reporting.GatlingLogFormatter.GATLING_HEADLINE_TEMPLATE;
import static io.ryos.rhino.sdk.utils.ReflectionUtils.getClassLevelAnnotation;
import static io.ryos.rhino.sdk.utils.ReflectionUtils.getFieldByAnnotation;
import static io.ryos.rhino.sdk.utils.ReflectionUtils.instanceOf;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import io.ryos.rhino.sdk.annotations.Feeder;
import io.ryos.rhino.sdk.annotations.Logging;
import io.ryos.rhino.sdk.annotations.SessionFeeder;
import io.ryos.rhino.sdk.annotations.UserFeeder;
import io.ryos.rhino.sdk.data.InjectionPoint;
import io.ryos.rhino.sdk.data.Pair;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.feeders.Feed;
import io.ryos.rhino.sdk.io.InfluxDBWriter;
import io.ryos.rhino.sdk.io.LogWriter;
import io.ryos.rhino.sdk.reporting.GatlingLogFormatter;
import io.ryos.rhino.sdk.reporting.LogFormatter;
import io.ryos.rhino.sdk.reporting.Recorder;
import io.ryos.rhino.sdk.reporting.RecorderImpl;
import io.ryos.rhino.sdk.reporting.StdoutReporter;
import io.ryos.rhino.sdk.reporting.StdoutReporter.EndTestEvent;
import io.ryos.rhino.sdk.reporting.UserEvent;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.repositories.UserRepository;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.concurrent.Await;
import scala.concurrent.duration.FiniteDuration;

/**
 * {@link Simulation} is representation of a single performance testing job. The instances of {@link
 * Simulation} is created by using the metadata provided on annotated benchmark entities. Simulation
 * entities do comprise scenarios, that are run per user on a single thread. For each scenario there
 * will be a new Simulation instance created so as to run the scenario isolated on a single thread.
 * <p>
 *
 * The job instances are created by {@link SimulationJobsScanner} classes.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see io.ryos.rhino.sdk.annotations.Simulation
 * @since 1.0.0
 */
public class Simulation {

  private static final String ACTOR_SYS_NAME = "benchmark";
  private static final Logger LOG = LogManager.getLogger(Simulation.class);

  /**
   * The name of the simulation, used in reports as well as to start a specific simulation among
   * others, selectively.
   * <p>
   */
  private String simulationName;

  /**
   * Duration of the simulation in minutes.
   * <p>
   */
  private int duration;

  /**
   * The number of users to be injected during the benchmark job execution. It is the maximum number
   * of users making benchmark requests against the back-end.
   * <p>
   */
  private int injectUser;

  /**
   * The number of users to be injected per second.
   * <p>
   */
  private int rampUp;

  /**
   * SimulationSpec class.
   * <p>
   */
  private Class simulationClass;

  /**
   * SimulationSpec object factory. All reflection calls should be run on this single instance.
   * <p>
   */
  private Supplier<Object> simulationInstanceFactory =
      () -> instanceOf(simulationClass).orElseThrow();

  /**
   * The {@link java.lang.reflect.Method} instance for running the test.
   * <p>
   */
  private List<Scenario> runnableScenarios;

  /**
   * The {@link java.lang.reflect.Method} instance for preparing the scenario.
   * <p>
   */
  private Method beforeMethod;

  /**
   * The {@link java.lang.reflect.Method} instance for cleaning up the scenario. The clean up method
   * will be run after scenario test execution.
   * <p>
   */
  private Method afterMethod;

  /**
   * The {@link java.lang.reflect.Method} instance for preparing the simulation.
   * <p>
   */
  private Method prepareMethod;

  /**
   * The {@link java.lang.reflect.Method} instance for cleaning up the test. The clean up method
   * will be run after performance simulation execution.
   * <p>
   */
  private Method cleanupMethod;

  /**
   * User repository.
   * <p>
   */
  private UserRepository<UserSession> userRepository;

  /**
   * Reporter actor reference is the reference to the actor which receives reporting events.
   * <p>
   */
  private ActorRef loggerActor;

  /**
   * Reporter actor reference to report log events to the Influx DB.
   * <p>
   */
  private ActorRef influxActor;

  /**
   * StdOut reporter is to write out about the test execution to the stdout. It can be considered as
   * heartbeat about the running test.
   * <p>
   */
  private ActorRef stdOutReptorter;

  /**
   * Enable Influx DB integration.
   * <p>
   */
  private boolean enableInflux;

  private ActorSystem system = ActorSystem.create(ACTOR_SYS_NAME);

  // Predicate to search fields for Feeder annotation.
  private final Predicate<Field> hasFeeder = (f) -> Arrays
      .stream(f.getDeclaredAnnotations())
      .anyMatch(Feeder.class::isInstance);

  private final Function<Field, InjectionPoint<Feeder>> ipCreator =
      (f) -> new InjectionPoint<>(f, f.getDeclaredAnnotation(Feeder.class));

  // Feed the feeder value into the field.
  private void feed(final Object instance, final InjectionPoint<Feeder> ip) {
    Feed o = instanceOf(ip.getAnnotation().factory()).orElseThrow();
    Object value = o.take();
    try {
      Field field = ip.getField();
      field.setAccessible(true);
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      LOG.error("Feeder's return type and field's type is not compatible: " + e.getMessage());
    }
  }

  /* Find the first annotation type, clazzAnnotation, on field declarations of the clazz.  */
  private void feedInjections(Object simulationInstance) {
    Arrays.stream(simulationClass.getDeclaredFields())
        .filter(hasFeeder)
        .map(ipCreator)
        .forEach(ip -> feed(simulationInstance, ip));
  }

  /*
   * Uses a builder to construct the instance.
   */
  private Simulation(final Builder builder) {
    this.duration = builder.duration;
    this.simulationName = builder.simulation;
    this.injectUser = builder.injectUser;
    this.rampUp = builder.rampUp;
    this.simulationClass = builder.simulationClass;
    this.runnableScenarios = builder.runnerMethod;
    this.prepareMethod = builder.prepareMethod;
    this.cleanupMethod = builder.cleanUpMethod;
    this.beforeMethod = builder.beforeMethod;
    this.afterMethod = builder.afterMethod;
    this.userRepository = builder.userRepository;
    this.enableInflux = builder.enableInflux;

    /*
     * Log writer is the {@link java.io.Closeable} instance to write the execution
     * logs. The log contains the metrics about the test run as well as the
     * information to identify the tests.
     */
    final String reportingURI = builder.reportingURI;
    final LogFormatter formatter = getLogFormatter();

    this.stdOutReptorter = system.actorOf(StdoutReporter.props(injectUser, Instant.now(), duration),
        StdoutReporter.class.getName());
    this.loggerActor = system.actorOf(LogWriter.props(reportingURI, formatter),
        LogWriter.class.getName());

    if (enableInflux) {
      influxActor = system.actorOf(InfluxDBWriter.props(), InfluxDBWriter.class.getName());
    }

    if (formatter instanceof GatlingLogFormatter) {
      loggerActor.tell(
          String.format(
              GATLING_HEADLINE_TEMPLATE,
              simulationClass.getName(),
              simulationName,
              System.currentTimeMillis(),
              GatlingLogFormatter.GATLING_VERSION),
          ActorRef.noSender());
    }
  }

  private LogFormatter getLogFormatter() {
    final Optional<Logging> loggingAnnotation = getClassLevelAnnotation(simulationClass,
        Logging.class);
    final Logging logging = loggingAnnotation.orElseGet(() -> null);

    if (logging == null) {
      return null;
    }

    final Optional<? extends LogFormatter> logFormatterInstance = instanceOf(logging.formatter());
    return logFormatterInstance.orElseThrow(RuntimeException::new);
  }

  void prepare(UserSession userSession) {
    final Object cleanUpInstance = prepareMethodCall(userSession);
    executeMethod(prepareMethod, cleanUpInstance);
  }

  private Object prepareMethodCall(final UserSession userSession) {
    final Object cleanUpInstance = simulationInstanceFactory.get();
    injectSession(userSession, cleanUpInstance);
    feedInjections(cleanUpInstance);
    injectUser(userSession.getUser(), cleanUpInstance);
    return cleanUpInstance;
  }

  void cleanUp(UserSession userSession) {
    prepareMethodCall(userSession);
    executeMethod(cleanupMethod, userSession);
  }

  private void executeMethod(final Method method, final Object simulationInstance) {
    try {
      if (method != null) {
        method.invoke(simulationInstance);
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      LOG.error(e.getCause().getMessage());
      throw new RuntimeException(
          "Cannot invoke the method with step: " + method.getName() + "()", e);
    }
  }

  private void executeScenario(final Scenario scenario, final RecorderImpl recorder,
      final Object simulationInstance) {
    try {
      scenario.getMethod().invoke(simulationInstance, recorder);
    } catch (IllegalAccessException | InvocationTargetException e) {
      LOG.error(e.getCause().getMessage());
      throw new RuntimeException(e.getCause());
    }
  }

  private void injectSession(final UserSession userSession, final Object simulationInstance) {
    final Optional<Pair<Field, SessionFeeder>> fieldAnnotation = getFieldByAnnotation(
        simulationClass,
        SessionFeeder.class);
    fieldAnnotation
        .ifPresent(f -> setValueToInjectionPoint(userSession, f.getFirst(), simulationInstance));
  }

  private void injectUser(final User user, final Object simulationInstance) {
    final Optional<Pair<Field, UserFeeder>> fieldAnnotation = getFieldByAnnotation(simulationClass,
        UserFeeder.class);
    fieldAnnotation.ifPresent(f -> setValueToInjectionPoint(user, f.getFirst(), simulationInstance));
  }

  private <T> void setValueToInjectionPoint(final T object, final Field f,
      final Object simulationInstance) {
    try {
      f.setAccessible(true);
      f.set(simulationInstance, object);
    } catch (IllegalAccessException e) {
      LOG.error(e);
      //TODO
    }
  }

  /**
   * The method runs the scenarios for a user provided.
   *
   * @param userSession User session, to be injected.
   * @param scenario Scenario, to be run.
   * @return Recorder instance which contains simulation logs.
   */
  public Recorder run(final UserSession userSession, final Scenario scenario) {
    final User user = userSession.getUser();
    final Object simulationInstance = simulationInstanceFactory.get();

    injectUser(user, simulationInstance);// Each thread will run as the same user.
    injectSession(userSession, simulationInstance);

    executeMethod(beforeMethod, simulationInstance);

    feedInjections(simulationInstance);

    var recorder = new RecorderImpl(scenario.getDescription(), user.getId());
    var start = System.currentTimeMillis();

    final UserEvent userEventStart = new UserEvent();
    userEventStart.elapsed = 0;
    userEventStart.start = start;
    userEventStart.end = start;
    userEventStart.scenario = scenario.getDescription();
    userEventStart.eventType = "START";
    userEventStart.id = user.getId();

    recorder.record(userEventStart);

    executeScenario(scenario, recorder, simulationInstance);

    var elapsed = System.currentTimeMillis() - start;

    final UserEvent userEventEnd = new UserEvent();
    userEventEnd.elapsed = elapsed;
    userEventEnd.start = start;
    userEventEnd.end = start + elapsed;
    userEventEnd.scenario = scenario.getDescription();
    userEventEnd.eventType = "END";
    userEventEnd.id = user.getId();
    recorder.record(userEventEnd);

    dispatchEvents(recorder);

    executeMethod(afterMethod, simulationInstance);

    return recorder;
  }

  private void dispatchEvents(final RecorderImpl recorder) {
    recorder.getEvents().forEach(e -> {

      loggerActor.tell(e, ActorRef.noSender());

      stdOutReptorter.tell(e, ActorRef.noSender());

      if (enableInflux) {
        influxActor.tell(e, ActorRef.noSender());
      }
    });
  }

  void stop() {

    reportTermination();

    var terminate = system.terminate();

    terminate.onComplete(new OnComplete<>() {

      @Override
      public void onComplete(final Throwable throwable, final Terminated terminated) {
        system = null;
      }

    }, system.dispatcher());
  }

  private void reportTermination() {
    var ask = Patterns.ask(stdOutReptorter, new EndTestEvent(Instant.now()), 5000L);
    try {
      Await.result(ask, FiniteDuration.Inf());
    } catch (Exception e) {
      LOG.debug(e); // expected exception is a Timeout. It is ok.
    }
  }

  int getInjectUser() {
    return injectUser;
  }

  UserRepository<UserSession> getUserRepository() {
    return userRepository;
  }

  int getDuration() {
    return duration;
  }

  List<Scenario> getRunnableScenarios() {
    return runnableScenarios;
  }

  /**
   * Builder for {@link Simulation}.
   */
  static class Builder {

    /**
     * Simulation simulationName.
     */
    private String simulation;

    /**
     * The total number of users to be injected.
     */
    private int injectUser;

    /**
     * Ramp up count defines the number of users being injected per second.
     */
    private int rampUp;

    /**
     * SimulationSpec class, is the one with the {@link io.ryos.rhino.sdk.annotations.Simulation}
     * annotation.
     */
    private Class<?> simulationClass;

    /**
     * The {@link java.lang.reflect.Method} instance of the run method.
     */
    private List<Scenario> runnerMethod;

    /**
     * The {@link java.lang.reflect.Method} instance for preparing the test.
     */
    private Method prepareMethod;

    /**
     * The {@link java.lang.reflect.Method} instance for cleaning up the test. The clean up method
     * will be run after performance test execution.
     */
    private Method cleanUpMethod;

    /**
     * The {@link java.lang.reflect.Method} instance for preparing the test.
     */
    private Method beforeMethod;

    /**
     * The {@link java.lang.reflect.Method} instance for cleaning up the test. The clean up method
     * will be run after performance test execution.
     */
    private Method afterMethod;

    /**
     * User repository.
     */
    private UserRepository<UserSession> userRepository;

    /**
     * The reporting URI in String.
     */
    private String reportingURI;

    /**
     * Log formatter.
     */
    private String logFormatter;

    /**
     * Duration of the performance test.
     */
    private int duration;

    /**
     * Enables the influx db integration.
     */
    private boolean enableInflux;

    public Builder withInflux(final boolean enableInflux) {
      this.enableInflux = enableInflux;
      return this;
    }

    public Builder withSimulation(final String simulation) {
      this.simulation = simulation;
      return this;
    }

    public Builder withInjectUser(final int injectUser) {
      this.injectUser = injectUser;
      return this;
    }

    public Builder withRampUp(final int rampUp) {
      this.rampUp = rampUp;
      return this;
    }

    public Builder withSimulationClass(final Class<?> simulationClass) {
      this.simulationClass = simulationClass;
      return this;
    }

    public Builder withScenarios(final List<Scenario> scenarios) {
      this.runnerMethod = scenarios;
      return this;
    }

    public Builder withPrepare(final Method prepareMethod) {
      this.prepareMethod = prepareMethod;
      return this;
    }

    public Builder withCleanUp(final Method cleanUpMethod) {
      this.cleanUpMethod = cleanUpMethod;
      return this;
    }

    public Builder withBefore(final Method beforeMethod) {
      this.beforeMethod = beforeMethod;
      return this;
    }

    public Builder withAfter(final Method afterMethod) {
      this.afterMethod = afterMethod;
      return this;
    }

    public Builder withUserRepository(final UserRepository<UserSession> userRepository) {
      this.userRepository = userRepository;
      return this;
    }

    public Builder withLogWriter(final String reportingURI) {
      this.reportingURI = reportingURI;
      return this;
    }

    public Builder withLogFormatter(final String logWriter) {
      this.reportingURI = logWriter;
      return this;
    }

    public Builder withDuration(final int duration) {
      this.duration = duration;
      return this;
    }

    public Simulation build() {
      return new Simulation(this);
    }
  }
}