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

package io.ryos.rhino.sdk.runners;

import static reactor.core.publisher.Flux.fromStream;

import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.data.Context;
import io.ryos.rhino.sdk.data.ContextImpl;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.io.Out;
import io.ryos.rhino.sdk.monitoring.GrafanaGateway;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.repositories.RegionalUserProvider;
import io.ryos.rhino.sdk.users.repositories.RegionalUserProviderImpl;
import io.ryos.rhino.sdk.users.repositories.UserRepository;
import io.ryos.rhino.sdk.utils.ReflectionUtils;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * Push-based simulation runner spawns number of threads as it is configured in the properties file.
 * The source of the runner is the {@link CyclicIterator} which streams users zipped with scenarios,
 * that are the entities containing the load generation algorithm. Every time a new user is streamed
 * through the pipeline, the scenario will be executed with the user.
 * <p>
 *
 * The caveat is using push-based approach is that the threads can be blocked, once the the load
 * testing implementation is blocking. Alternative approach is the reactive one in which the
 * framework provides a DSL. The load DSL enables developers to write load tests in declarative
 * fashion.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see CyclicIterator
 * @see ReactiveHttpSimulationRunner
 * @since 1.0.0
 */
public class DefaultSimulationRunner implements SimulationRunner {

  private static final String JOB = "job";
  private static final long ONE_SEC = 1000L;
  private static final long MAX_WAIT_FOR_USER = 60;

  private final SimulationMetadata simulationMetadata;
  private final CyclicIterator<Scenario> scenarioCyclicIterator;
  private final ScheduledExecutorService scheduler;
  private final EventDispatcher eventDispatcher;

  private volatile boolean shutdownInitiated;
  private volatile Disposable subscribe;

  /**
   * Creates a new {@link DefaultSimulationRunner} instance.
   * <p>
   *
   * @param context {@link ContextImpl} instance.
   */
  public DefaultSimulationRunner(Context context) {
    this.simulationMetadata = context.<SimulationMetadata>get(JOB).orElseThrow();
    this.scenarioCyclicIterator = new CyclicIterator<>(simulationMetadata.getScenarios());
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.eventDispatcher = new EventDispatcher(simulationMetadata);
  }

  public void start() {

    Out.info(
        "Starting load test for " + simulationMetadata.getDuration().toMinutes() + " minutes ...");

    var userRepository = simulationMetadata.getUserRepository();
    var userProvider = new RegionalUserProviderImpl(userRepository,
        simulationMetadata.getUserRegion());
    if (SimulationConfig.isGrafanaEnabled()) {
      Out.info("Grafana is enabled. Creating dashboard: " + SimulationConfig.getSimulationId());
      new GrafanaGateway().setUpDashboard(SimulationConfig.getSimulationId(),
          simulationMetadata.getScenarios()
              .stream()
              .map(Scenario::getDescription)
              .toArray(String[]::new));
    }

    // We need to wait till all users are logged in.
    waitUsers(userProvider);
    prepareUserSessions();

    new DefaultRunnerSimulationInjector(simulationMetadata, null)
        .injectOn(simulationMetadata.getTestInstance());

    var users = Stream.generate(userProvider::take);
    var scenarios = Stream.generate(scenarioCyclicIterator::next);

    this.subscribe = Flux.zip(fromStream(users), fromStream(scenarios))
        .take((simulationMetadata.getDuration()))
        .parallel(SimulationConfig.getParallelisation())
        .runOn(Schedulers.elastic())
        .doOnTerminate(this::notifyAwaiting)
        .doOnNext(t -> new DefaultSimulationCallable(simulationMetadata, t.getT1(), t.getT2(),
            eventDispatcher).call())
        .subscribe();

    await();
    stop();
  }

  private void await() {
    synchronized (this) {
      try {
        wait(simulationMetadata.getDuration().toMillis() + 1000);
      } catch (InterruptedException e) {
        // Intentionally left empty.
      }
    }
  }

  private void notifyAwaiting() {
    synchronized (this) {
      notify();
    }
  }

  @Override
  public void stop() {

    System.out.println("Someone pushed the stop() button on runner.");
    shutdown();
  }

  private void shutdown() {
    if (shutdownInitiated) {
      return;
    }

    shutdownInitiated = true;

    System.out.println("Stopping the simulation...");

    subscribe.dispose();
    // run cleanup.
    System.out.println("Cleaning up.");
    final UserRepository<UserSession> userRepository = simulationMetadata.getUserRepository();
    cleanupUserSessions();

    // proceed with shutdown.
    System.out.println("Shutting down the system ...");
    scenarioCyclicIterator.stop();
    eventDispatcher.stop();

    System.out.println("Shutting down the scheduler ...");
    scheduler.shutdown();
    int retry = 0;
    while (!scheduler.isShutdown() && ++retry < 5) {
      waitForASec();
    }

    scheduler.shutdownNow();

    System.out.println("Shutting down completed ...");
    System.out.println("Bye!");
  }

  private void waitForASec() {
    System.out.println("Wait ...");
    try {
      Thread.sleep(DefaultSimulationRunner.ONE_SEC);
    } catch (InterruptedException e) {
      // intentionally left empty.
    }
  }

  private void prepareUserSessions() {
    if (simulationMetadata.getPrepareMethod() != null) {
      ReflectionUtils.executeMethod(simulationMetadata.getPrepareMethod(),
          simulationMetadata.getTestInstance());
    }
  }

  private void cleanupUserSessions() {
    if (simulationMetadata.getCleanupMethod() != null) {
      ReflectionUtils.executeMethod(simulationMetadata.getCleanupMethod(),
          simulationMetadata.getTestInstance());
    }
  }

  private void waitUsers(final RegionalUserProvider<UserSession> userProvider) {

    int retry = 0;
    while (!userProvider.has(simulationMetadata.getNumberOfUsers())
        && ++retry < MAX_WAIT_FOR_USER) {

      System.out.println("? Not sufficient user has been logged in. Required " + simulationMetadata
          .getNumberOfUsers() + "Waiting...");
      waitForASec();
    }

    if (!userProvider.has(simulationMetadata.getNumberOfUsers())) {
      System.out.println(
          "? Not sufficient user in user repository found to be able to run the " + "in "
              + "similation. Check your user source, or reduce the number of max. user the simulation requires "
              + "@Simulation annotation. Required "
              + simulationMetadata.getNumberOfUsers() + " user.");

      shutdown();
      System.exit(-1);
    }

    System.out
        .println("User login completed. Total user: " + simulationMetadata.getNumberOfUsers());
  }
}
