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

import static io.ryos.rhino.sdk.utils.ReflectionUtils.instanceOf;

import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.data.Context;
import io.ryos.rhino.sdk.data.ContextImpl;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner.Action;
import io.ryos.rhino.sdk.users.repositories.CyclicUserSessionRepositoryImpl;
import io.ryos.rhino.sdk.utils.ReflectionUtils;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
public class DefaultSimulationRunner extends AbstractSimulationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultSimulationRunner.class);
  private static final String JOB = "job";
  private static final long ONE_SEC = 1000L;

  private final ScheduledExecutorService scheduler;
  private final EventDispatcher eventDispatcher;

  private final Condition continueCondition;
  private final Lock masterLock;

  private volatile boolean shutdownInitiated;
  private Disposable subscribe;
  private volatile boolean isPipelineCompleted;

  /**
   * Creates a new {@link DefaultSimulationRunner} instance.
   * <p>
   *
   * @param context {@link ContextImpl} instance.
   */
  public DefaultSimulationRunner(Context context) {
    super(context.<SimulationMetadata>get(JOB).orElseThrow());
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.eventDispatcher = new EventDispatcher(getSimulationMetadata());
    this.masterLock = new ReentrantLock();
    this.continueCondition = masterLock.newCondition();
  }

  public void start() {

    var simulationMetadata = getSimulationMetadata();
    LOG.info("Starting load test for {} minutes ...", simulationMetadata.getDuration().toMinutes());

    var userRepository = simulationMetadata.getUserRepository();
    var userSessionProvider = new CyclicUserSessionRepositoryImpl(userRepository,
        simulationMetadata.getUserRegion(),
        simulationMetadata.getNumberOfUsers());

    if (simulationMetadata.getGrafanaInfo() != null) {
      setUpGrafanaDashboard();
    }

    prepareUserSessions(userSessionProvider.getUserList());

    var users = Stream.generate(userSessionProvider::take);
    var flux = Flux.fromStream(users);
    flux = appendRampUp(flux);
    flux = appendThrottling(flux);
    flux = appendTake(flux);

    var injector = new DefaultRunnerSimulationInjector(simulationMetadata);

    this.subscribe = flux.onErrorResume(t -> Mono.empty())
        .parallel(SimulationConfig.getParallelisation())
        .runOn(Schedulers.elastic())
        .doOnTerminate(this::shutdown)
        .doOnNext(userSession -> {
          var instance = instanceOf(simulationMetadata.getSimulationClass()).orElseThrow();
          injector.injectOn(instanceOf(simulationMetadata.getSimulationClass()).orElseThrow());
          // Run the scenario subsequently.
          simulationMetadata.getScenarios().forEach(scenario ->
              new DefaultSimulationCallable(simulationMetadata, userSession, scenario,
                  eventDispatcher, instance).call());
        })
        .doOnComplete(() -> signalCompletion(() -> isPipelineCompleted = true))
        .subscribe();

    awaitIf(() -> !isPipelineCompleted);

    LOG.info("Cleaning up ...");
    cleanupUserSessions(userSessionProvider.getUserList());

    shutdown();
    LOG.info("Bye!");
  }

  private void signalCompletion(Action action) {
    try {
      masterLock.lock();
      action.execute();
      continueCondition.signal();
    } finally {
      masterLock.unlock();
    }
  }

  private void awaitIf(Supplier<Boolean> supplier) {
    while (supplier.get()) {
      try {
        masterLock.lock();
        continueCondition.await(1, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } finally {
        masterLock.unlock();
      }
    }
  }

  private void prepareUserSessions(List<UserSession> userSessionList) {
    if (getSimulationMetadata().getPrepareMethod() != null) {
      LOG.info("Preparation started.");
      userSessionList.forEach(userSession -> ReflectionUtils.executeStaticMethod(
          getSimulationMetadata().getPrepareMethod(),
          userSession.getSimulationSession()));
      LOG.info("Preparation completed.");
    }
  }

  private void cleanupUserSessions(List<UserSession> userSessionList) {
    if (getSimulationMetadata().getCleanupMethod() != null) {
      LOG.info("Clean-up started.");
      userSessionList.forEach(userSession -> {
        ReflectionUtils.executeStaticMethod(
            getSimulationMetadata().getCleanupMethod(),
            userSession.getSimulationSession());
        userSession.empty();
      });
      LOG.info("Clean-up completed.");
    }
  }

  @Override
  public void stop() {

    LOG.info("Someone pushed the stop() button on runner.");
    shutdown();
  }

  private void shutdown() {
    if (shutdownInitiated) {
      return;
    }

    shutdownInitiated = true;

    LOG.info("Stopping the simulation...");

    subscribe.dispose();

    // proceed with shutdown.
    LOG.info("Shutting down the system ...");
    eventDispatcher.stop();
    scheduler.shutdown();

    int retry = 0;
    while (!scheduler.isShutdown() && ++retry < 5) {
      waitForASec();
    }

    scheduler.shutdownNow();

    LOG.info("Shutting down completed ...");
  }

  private void waitForASec() {
    LOG.info("Wait ...");
    try {
      Thread.sleep(DefaultSimulationRunner.ONE_SEC);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
