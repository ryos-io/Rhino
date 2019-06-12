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

import static io.ryos.rhino.sdk.utils.ReflectionUtils.getClassLevelAnnotation;
import static io.ryos.rhino.sdk.utils.ReflectionUtils.getFieldByAnnotation;
import static io.ryos.rhino.sdk.utils.ReflectionUtils.instanceOf;

import io.ryos.rhino.sdk.annotations.Logging;
import io.ryos.rhino.sdk.annotations.SessionFeeder;
import io.ryos.rhino.sdk.annotations.UserFeeder;
import io.ryos.rhino.sdk.data.InjectionPoint;
import io.ryos.rhino.sdk.data.Pair;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.feeders.Feedable;
import io.ryos.rhino.sdk.reporting.LogFormatter;
import io.ryos.rhino.sdk.runners.SimulationRunner;
import io.ryos.rhino.sdk.specs.Spec;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.repositories.UserRepository;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link SimulationMetadata} is representation of a single performance testing job. The instances of {@link
 * SimulationMetadata} is created by using the metadata provided on annotated benchmark entities.
 * Simulation metadata entities do comprise scenarios, that are run per user on a single thread.
 * For each scenario there will be a new SimulationMetadata instance created so as to run the
 * scenario isolated on a single thread.
 * <p>
 *
 * The job instances are created by {@link SimulationJobsScanner} classes.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see io.ryos.rhino.sdk.annotations.Simulation
 * @since 1.0.0
 */
public class SimulationMetadata {

  private static final String ACTOR_SYS_NAME = "benchmark";
  private static final Logger LOG = LogManager.getLogger(SimulationMetadata.class);

  /**
   * Runner.
   */
  private Class<? extends SimulationRunner> runner;

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
  private Duration duration;

  /**
   * The number of users to be injected during the benchmark job execution. It is the maximum number
   * of users making benchmark requests against the back-end.
   * <p>
   */
  private int numberOfUsers;

  /**
   * The number of users to be injected per second.
   * <p>
   */
  private int rampUp;

  /**
   * Simulation class.
   * <p>
   */
  private Class simulationClass;

  /**
   * Simulation object factory. All reflection calls should be run on this single instance.
   * <p>
   */
  private Supplier<Object> simulationInstanceFactory =
      () -> instanceOf(simulationClass).orElseThrow();

  /**
   * The {@link java.lang.reflect.Method} instance for running the test.
   * <p>
   */
  private List<Scenario> scenarios;

  /**
   * A list of {@link Spec} instances defined in specs methods.
   * <p>
   */
  private List<Spec> specs;

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
   * Enable Influx DB integration.
   * <p>
   */
  private boolean enableInflux;

  private String reportingURI;

  // Predicate to search fields for Feedable annotation.
  private final Predicate<Field> hasFeeder = f -> Arrays
      .stream(f.getDeclaredAnnotations())
      .anyMatch(io.ryos.rhino.sdk.annotations.Feeder.class::isInstance);

  private final Function<Field, InjectionPoint<io.ryos.rhino.sdk.annotations.Feeder>> ipCreator =
      f -> new InjectionPoint<>(f,
          f.getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.Feeder.class));

  // Feedable the feeder value into the field.
  private void feed(final Object instance,
      final InjectionPoint<io.ryos.rhino.sdk.annotations.Feeder> ip) {
    Feedable o = instanceOf(ip.getAnnotation().factory()).orElseThrow();
    Object value = o.take();
    try {
      Field field = ip.getField();
      field.setAccessible(true);
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      LOG.error("Feedable's return type and field's type is not compatible: " + e.getMessage());
    }
  }

  /* Find the first annotation type, clazzAnnotation, on field declarations of the clazz.  */
  private void feedInjections(Object simulationInstance) {
    Arrays.stream(simulationClass.getDeclaredFields())
        .filter(hasFeeder)
        .map(ipCreator)
        .forEach(ip -> feed(simulationInstance, ip));
  }

  /**
   * Uses a builder to construct the instance.
   * <p>
   */
  private SimulationMetadata(final Builder builder) {
    this.duration = builder.duration;
    this.simulationName = builder.simulation;
    this.numberOfUsers = builder.injectUser;
    this.rampUp = builder.rampUp;
    this.simulationClass = builder.simulationClass;
    this.scenarios = builder.scenarios;
    this.specs = builder.specs;
    this.prepareMethod = builder.prepareMethod;
    this.cleanupMethod = builder.cleanUpMethod;
    this.beforeMethod = builder.beforeMethod;
    this.afterMethod = builder.afterMethod;
    this.userRepository = builder.userRepository;
    this.enableInflux = builder.enableInflux;
    this.runner = builder.runner;
    this.reportingURI = builder.reportingURI;

  }

  public LogFormatter getLogFormatter() {
    var loggingAnnotation = getClassLevelAnnotation(simulationClass, Logging.class);
    var logging = loggingAnnotation.orElseGet(() -> null);

    if (logging == null) {
      return null;
    }

    final Optional<? extends LogFormatter> logFormatterInstance = instanceOf(logging.formatter());
    return logFormatterInstance.orElseThrow(RuntimeException::new);
  }

  public void prepare(UserSession userSession) {
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

  public void cleanUp(UserSession userSession) {
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
    fieldAnnotation
        .ifPresent(f -> setValueToInjectionPoint(user, f.getFirst(), simulationInstance));
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

  public Method getPrepareMethod() {
    return prepareMethod;
  }

  public Method getCleanupMethod() {
    return cleanupMethod;
  }

  public String getReportingURI() {
    return reportingURI;
  }

  public Class<? extends SimulationRunner> getRunner() {
    return runner;
  }

  public int getNumberOfUsers() {
    return numberOfUsers;
  }

  public UserRepository<UserSession> getUserRepository() {
    return userRepository;
  }

  public Duration getDuration() {
    return duration;
  }

  public List<Scenario> getScenarios() {
    return scenarios;
  }

  public List<Spec> getSpecs() {
    return specs;
  }

  public Class getSimulationClass() {
    return simulationClass;
  }

  public boolean isEnableInflux() {
    return enableInflux;
  }

  public Method getBeforeMethod() {
    return beforeMethod;
  }

  public Method getAfterMethod() {
    return afterMethod;
  }

  public String getSimulationName() {
    return simulationName;
  }

  /**
   * Builder for {@link SimulationMetadata}.
   */
  static class Builder {

    /**
     * Simulation name.
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
     * Simulation class, is the one with the {@link io.ryos.rhino.sdk.annotations.Simulation}
     * annotation.
     */
    private Class<?> simulationClass;

    /**
     * Runner implementation.
     */
    private Class<? extends SimulationRunner> runner;

    /**
     * The {@link java.lang.reflect.Method} instance of the run method.
     */
    private List<Scenario> scenarios;

    /**
     * List of {@link Spec} instances.
     */
    private List<Spec> specs;


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
    private Duration duration;

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

    public Builder withRunner(final Class<? extends SimulationRunner> runner) {
      this.runner = runner;
      return this;
    }

    public Builder withScenarios(final List<Scenario> scenarios) {
      this.scenarios = scenarios;
      return this;
    }

    public Builder withSpecs(final List<Spec> specs) {
      this.specs = specs;
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

    public Builder withDuration(final Duration duration) {
      this.duration = duration;
      return this;
    }

    public SimulationMetadata build() {
      return new SimulationMetadata(this);
    }
  }
}