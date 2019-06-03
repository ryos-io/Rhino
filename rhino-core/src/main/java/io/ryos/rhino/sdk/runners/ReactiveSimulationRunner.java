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

import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.Simulation;
import io.ryos.rhino.sdk.data.Context;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.specs.Spec;
import io.ryos.rhino.sdk.users.repositories.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClientResponse;

public class ReactiveSimulationRunner implements SimulationRunner {

  private static final String JOB = "job";
  private static final long ONE_SEC = 1000L;
  private static final long MAX_WAIT_FOR_USER = 60;

  private final Context context;
  private Simulation simulation;
  private CyclicIterator<Scenario> scenarioCyclicIterator;
  private Disposable subscribe;
  private volatile boolean shutdownInitiated;

  public ReactiveSimulationRunner(final Context context) {
    this.context = context;
    this.simulation = context.<Simulation>get(JOB).orElseThrow();
    this.scenarioCyclicIterator = new CyclicIterator<>(simulation.getScenarios());
  }

  public void start() {

    System.out.println("Starting load test for " + simulation.getDuration() + " minutes ...");

    var userRepository = simulation.getUserRepository();

    // We need to wait till all users are logged in.
    waitUsers(userRepository);

    prepareUserSessions(userRepository.getUserSessions());

    this.subscribe = Flux.fromStream(Stream.generate(userRepository::take))
        .take((simulation.getDuration()))
        .parallel()
        .runOn(Schedulers.elastic())
        .doOnTerminate(this::notifyAwaiting)
        .flatMap(user -> Flux
                .fromStream(simulation.getSpecs().stream())
                .parallel()
                .runOn(Schedulers.elastic())
                .flatMap(spec -> materializeWith(user, spec))
        )
        .subscribe(this::dispatch);

    await();
    stop();
  }

  private void dispatch(HttpClientResponse clientResponse) {
    System.out.println(clientResponse.status().code());
  }

  private Mono<HttpClientResponse> materializeWith(final UserSession userSession, final Spec spec) {
    Objects.requireNonNull(spec);
    return spec.toMono();
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

    System.out.println("Shutting down completed ...");
    System.out.println("Bye!");
  }

  private void waitForASec() {
    System.out.println("Wait ...");
    try {
      Thread.sleep(ONE_SEC);
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
