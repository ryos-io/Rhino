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

import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.dispatch.OnComplete;
import akka.stream.ActorMaterializer;
import akka.stream.KillSwitches;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.StreamConverters;
import io.ryos.rhino.sdk.data.ContextImpl;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.io.CyclicIterator;
import io.ryos.rhino.sdk.users.repositories.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import scala.concurrent.Future;

/**
 * Simulation runner is the load generator engine based on Akka streams. The implementation
 * creates two cyclic iterator, one of them is for {@link UserSession} instances, the other one
 * is for {@link Scenario} instances, both of them will be zipped into Pair during stream
 * processing. Cyclic iterators delivers {@link UserSession} and {@link Scenario} instances
 * infinitely, unless the stop method is explicitly called. The simulation will be run with
 * UserSession and Scenario pair, e.g <UserA, scenario1> in parallel. If the generators are not
 * stopped explicitly, the stream becomes a perpetual stream, that runs infinitely.
 * <p>
 *
 * The {@link SimulationRunner} terminates either by calling the stop method explicitly, or
 * the completes, it is the elapsed time exceeds the test duration defined in
 * {@link io.ryos.rhino.sdk.annotations.Simulation} annotation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see CyclicIterator
 * @since 1.0.0
 */
public class SimulationRunnerImpl implements SimulationRunner {

  private static final String JOB = "job";
  private static final long ONE_SEC = 1000L;
  private static final long MAX_WAIT_FOR_USER = 60;
  private static final int BUFFER_SIZE = 2000;
  private static final long INITIAL_DELAY = 0L;
  private static final long PERIOD = 1L;

  private Simulation simulation;
  private ActorSystem system = ActorSystem.create("rhino");
  private CyclicIterator<Scenario> scenarioCyclicIterator;
  private ScheduledExecutorService scheduler;
  private volatile long elapsed;
  private volatile int duration;
  private volatile boolean shutdownInitiated;

  /**
   * Creates a new {@link SimulationRunnerImpl} instance.
   * <p>
   *
   * @param context {@link ContextImpl} instance.
   */
  SimulationRunnerImpl(ContextImpl context) {
    this.simulation = context.<Simulation>get(JOB).orElseThrow();
    this.scenarioCyclicIterator = new CyclicIterator<>(simulation.getRunnableScenarios());
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.duration = simulation.getDuration();
  }

  public void start() {

    System.out.println("Starting load test for " + simulation.getDuration() + " minutes ...");

    final UserRepository userRepository = simulation.getUserRepository();

    // We need to wait till all users are logged in.
    waitUsers(userRepository);

    prepareUserSessions(userRepository.getUserSessions());

    // two streams will be zipped and materialized.
    var userStream =
        StreamConverters
            .fromJavaStream(() -> Stream.generate((Supplier<UserSession>) userRepository::take));
    var scenarioStream =
        StreamConverters.fromJavaStream(() -> Stream.generate(scenarioCyclicIterator::next));

    var materializer = ActorMaterializer.create(system);

    // Scheduler to determine the end of simulation.
    scheduler
        .scheduleAtFixedRate(this::shutdownIfCompleted, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);

    // Fetch users from circular linked-list, i.e infinite source.
    var doneCompletionStage = userStream.zip(scenarioStream)
        .viaMat(KillSwitches.single(), Keep.right())
        .buffer(BUFFER_SIZE, OverflowStrategy.backpressure())
        .takeWhile(p -> checkDuration())
        .map(p -> simulation.run(p.first(), p.second()))
        .async()
        .runWith(Sink.ignore(), materializer);

    doneCompletionStage.thenRun(() -> system.terminate());

    // Exceptional shutdown.
    doneCompletionStage.exceptionally(t -> {
      shutDown(-1);
      return null;
    });
  }

  private void shutdownIfCompleted() {
    synchronized (this) {
      ++elapsed;
    }
    if (isCompleted()) {
      System.out.println("! Performance test is now completed. Shutting down the system ...");
      shutDown(0);
    }
  }

  private boolean checkDuration() {
    synchronized (SimulationRunnerImpl.this) {
      return elapsed < duration * 60;
    }
  }

  private boolean isCompleted() {
    return elapsed >= duration * 60;
  }

  @Override
  public void stop() {
    stop(0);
  }

  public void stop(int statusCode) {
    System.out.println("Someone pushed the stop() button on runner.");
    shutDown(statusCode);
  }

  private void shutDown(int status) {
    if (shutdownInitiated) {
      return;
    }

    shutdownInitiated = true;

    System.out.println("Stopping the simulation...");

    final Future<Terminated> terminature = system.terminate();
    terminature.onComplete(new OnComplete<>() {
      @Override
      public void onComplete(final Throwable throwable, final Terminated terminated) {
        if (throwable != null) {
          // shutdown failed.
          System.err.println(throwable.getMessage());
        }

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

        System.exit(status);
      }
    }, system.dispatcher());
  }

  private void waitForASec() {
    System.out.println("Wait ...");
    try {
      Thread.sleep(SimulationRunnerImpl.ONE_SEC);
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

      shutDown(-1);
    }

    System.out.println("User login completed. Total user: " + simulation.getInjectUser());
  }
}
