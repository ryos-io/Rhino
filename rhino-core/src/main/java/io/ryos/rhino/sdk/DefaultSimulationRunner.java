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

import static reactor.core.publisher.Flux.fromStream;

import io.ryos.rhino.sdk.data.Context;
import io.ryos.rhino.sdk.data.ContextImpl;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.io.CyclicIterator;
import io.ryos.rhino.sdk.users.repositories.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * Simulation runner is the load generator engine based on Akka streams. The implementation creates
 * two cyclic iterator, one of them is for {@link UserSession} instances, the other one is for
 * {@link Scenario} instances, both of them will be zipped into Pair during stream processing.
 * Cyclic iterators delivers {@link UserSession} and {@link Scenario} instances infinitely, unless
 * the stop method is explicitly called. The simulation will be run with UserSession and Scenario
 * pair, e.g <UserA, scenario1> in parallel. If the generators are not stopped explicitly, the
 * stream becomes a perpetual stream, that runs infinitely.
 * <p>
 *
 * The {@link SimulationRunner} terminates either by calling the stop method explicitly, or the
 * completes, it is the elapsed time exceeds the test duration defined in {@link
 * io.ryos.rhino.sdk.annotations.Simulation} annotation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see CyclicIterator
 * @since 1.0.0
 */
public class DefaultSimulationRunner implements SimulationRunner {

  private static final String JOB = "job";
  private static final long ONE_SEC = 1000L;
  private static final long MAX_WAIT_FOR_USER = 60;

  private Simulation simulation;
  private CyclicIterator<Scenario> scenarioCyclicIterator;
  private ScheduledExecutorService scheduler;
  private volatile boolean shutdownInitiated;
  private Disposable subscribe;

  /**
   * Creates a new {@link DefaultSimulationRunner} instance.
   * <p>
   *
   * @param context {@link ContextImpl} instance.
   */
  public DefaultSimulationRunner(Context context) {
    this.simulation = context.<Simulation>get(JOB).orElseThrow();
    this.scenarioCyclicIterator = new CyclicIterator<>(simulation.getRunnableScenarios());
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
  }

  public void start() {

    System.out.println("Starting load test for " + simulation.getDuration() + " minutes ...");

    var userRepository = simulation.getUserRepository();

    // We need to wait till all users are logged in.
    waitUsers(userRepository);

    prepareUserSessions(userRepository.getUserSessions());

    var users = Stream.generate(userRepository::take);
    var scenarios = Stream.generate(scenarioCyclicIterator::next);

    this.subscribe = Flux.zip(fromStream(users), fromStream(scenarios))
        .take((simulation.getDuration()))
        .parallel(SimulationConfig.getParallelisation())
        .runOn(Schedulers.elastic())
        .doOnTerminate(this::notifyAwaiting)
        .doOnNext(t -> simulation.run(t.getT1(), t.getT2()))
        .subscribe();

    await();
    stop();
  }

  private void await() {
    synchronized (this) {
      try {
        wait(simulation.getDuration().toMillis() + 1000);
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
    final UserRepository<UserSession> userRepository = simulation.getUserRepository();
    cleanupUserSessions(userRepository.getUserSessions());

    // proceed with shutdown.
    System.out.println("Shutting down the system ...");
    scenarioCyclicIterator.stop();
    simulation.stop();

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

  private void prepareUserSessions(final List<UserSession> userSessions) {
    userSessions.forEach(us -> simulation.prepare(us));
  }

  private void cleanupUserSessions(final List<UserSession> userSessions) {
    userSessions.forEach(us -> simulation.cleanUp(us));
  }

  private void waitUsers(UserRepository userRepository) {
    Objects.requireNonNull(userRepository);

    int retry = 0;
    while (!userRepository.has(simulation.getInjectUser()) && ++retry < MAX_WAIT_FOR_USER) {
      System.out.println(
          "? Not sufficient user has been logged in. Required " + simulation.getInjectUser() + ". "
              + "Waiting...");
      waitForASec();
    }

    if (!userRepository.has(simulation.getInjectUser())) {
      System.out.println(
          "? Not sufficient user in user repository found to be able to run the " + "in "
              + "similation. Check your user source, or reduce the number of max. user the simulation requires "
              + "@Simulation annotation. Required "
              + simulation.getInjectUser() + " user.");

      shutdown();
      System.exit(-1);
    }

    System.out.println("User login completed. Total user: " + simulation.getInjectUser());
  }
}
