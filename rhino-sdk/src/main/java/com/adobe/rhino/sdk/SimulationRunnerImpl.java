/**************************************************************************
 *                                                                        *
 * ADOBE CONFIDENTIAL                                                     *
 * ___________________                                                    *
 *                                                                        *
 *  Copyright 2018 Adobe Systems Incorporated                             *
 *  All Rights Reserved.                                                  *
 *                                                                        *
 * NOTICE:  All information contained herein is, and remains              *
 * the property of Adobe Systems Incorporated and its suppliers,          *
 * if any.  The intellectual and technical concepts contained             *
 * herein are proprietary to Adobe Systems Incorporated and its           *
 * suppliers and are protected by trade secret or copyright law.          *
 * Dissemination of this information or reproduction of this material     *
 * is strictly forbidden unless prior written permission is obtained      *
 * from Adobe Systems Incorporated.                                       *
 **************************************************************************/

package com.adobe.rhino.sdk;

import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.dispatch.OnComplete;
import akka.stream.ActorMaterializer;
import akka.stream.KillSwitches;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.adobe.rhino.sdk.data.ContextImpl;
import com.adobe.rhino.sdk.data.Pair;
import com.adobe.rhino.sdk.data.Scenario;
import com.adobe.rhino.sdk.data.UserSession;
import com.adobe.rhino.sdk.io.CyclicIterator;
import com.adobe.rhino.sdk.users.UserRepository;
import com.google.common.collect.Streams;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.concurrent.Future;

public class SimulationRunnerImpl implements SimulationRunner {

  private static final Logger LOG = LogManager.getLogger(SimulationRunnerImpl.class);
  private static final String JOB = "job";
  private static final long ONE_SEC = 1000L;
  private static final int BUFFER_SIZE = 2000;
  private static final long INITIAL_DELAY = 0L;
  private static final long PERIOD = 1L;
  private static final int REPORTING_PERIOD = 5;

  private Simulation simulation;
  private ActorSystem system = ActorSystem.create("rhino");
  private CyclicIterator<Scenario> scenarioCyclicIterator;
  private ScheduledExecutorService scheduler;
  private volatile long elapsed;
  private volatile int duration;

  SimulationRunnerImpl(ContextImpl context) {
    this.simulation = context.<Simulation>get(JOB).orElseThrow();
    this.scenarioCyclicIterator = new CyclicIterator<>(simulation.getRunnableScenarios());
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.duration = simulation.getDuration();
  }

  public void start() {

    final UserRepository userRepository = simulation.getUserRepository();

    // We need to wait till all users are logged in.
    waitUsers(userRepository);

    prepareUserSessions(userRepository.getUserSessions());

    var userStream = Stream.generate((Supplier<UserSession>) userRepository::take);
    var scenarios = Stream.generate(scenarioCyclicIterator::next);
    var materializer = ActorMaterializer.create(system);

    scheduler.scheduleAtFixedRate(() -> {
      if (elapsed % REPORTING_PERIOD == 0) {
        System.out.println("* Ping? Pong! Running ... " + elapsed + " seconds.");
      }
      shutdownIfCompleted();
    }, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);

    // Fetch users from circular linked-list, i.e infinite source.
    var source = Source.from(Streams
        .zip(userStream, scenarios, (BiFunction<UserSession, Scenario, Pair>) Pair::new)::iterator)
        .viaMat(KillSwitches.single(), Keep.right());

    var doneCompletionStage = source.buffer(BUFFER_SIZE, OverflowStrategy.backpressure())
        .takeWhile((p) -> checkDuration())
        .map((p) -> CompletableFuture.supplyAsync(() -> simulation.run((UserSession) p.first, (Scenario) p.second), materializer.executionContext()))
        .async()
        .runWith(Sink.ignore(), materializer);

    doneCompletionStage.thenRun(system::terminate);

    // Exceptional shutdown.
    doneCompletionStage.exceptionally(t -> {

      final Future<Terminated> terminate = system.terminate();
      terminate.onComplete(new OnComplete<>() {
        @Override
        public void onComplete(final Throwable throwable, final Terminated terminated) {
          if (throwable != null) {
            System.err.println(throwable.getMessage());
          }

          System.exit(-1);
        }
      }, system.dispatcher());

      return null;
    });
  }

  private void shutdownIfCompleted() {

    synchronized (SimulationRunnerImpl.this) {

      if (++elapsed > duration * 60) {

        System.out.println("! Performance test is now completed. Shutting down the system ...");

        var terminate = system.terminate();

        terminate.onComplete(new OnComplete<>() {

          @Override
          public void onComplete(final Throwable throwable, final Terminated terminated) {
            System.exit(0);
          }
        }, system.dispatcher());

        simulation.stop();

        scheduler.shutdownNow();
      }
    }
  }

  private boolean checkDuration() {
    synchronized (SimulationRunnerImpl.this) {
      return elapsed < duration * 60;
    }
  }

  @Override
  public void stop() {
    scenarioCyclicIterator.stop();
  }

  private void prepareUserSessions(final List<UserSession> userSessions) {
    userSessions.forEach((us) -> simulation.prepare(us));
  }

  private void cleanupUserSessions(final List<UserSession> userSessions) {
    userSessions.forEach((us) -> simulation.cleanUp(us));
  }

  private void waitUsers(UserRepository userRepository) {
    while (userRepository != null && !userRepository.has(simulation.getInjectUser())) {
      System.out.println(
          "? Not sufficient user has been logged in. Required " + simulation.getInjectUser() + ". "
              + "Waiting...");
      try {
        Thread.sleep(ONE_SEC);
      } catch (InterruptedException e) {
        LOG.warn("Waiting for users interrupted.");
      }
    }

    System.out.println("! User login completed.");
  }
}
