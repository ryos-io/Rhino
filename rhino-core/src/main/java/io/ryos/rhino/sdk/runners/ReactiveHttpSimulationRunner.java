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

import static org.asynchttpclient.Dsl.get;
import static org.asynchttpclient.Dsl.head;

import com.google.common.collect.Streams;
import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.Simulation;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.data.Context;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.io.Out;
import io.ryos.rhino.sdk.monitoring.GrafanaGateway;
import io.ryos.rhino.sdk.specs.HttpSpec;
import io.ryos.rhino.sdk.specs.HttpSpecAsyncHandler;
import io.ryos.rhino.sdk.specs.Spec;
import io.ryos.rhino.sdk.users.data.OAuthUser;
import io.ryos.rhino.sdk.users.repositories.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.NotImplementedException;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.RequestBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ReactiveHttpSimulationRunner implements SimulationRunner {

  private static final String JOB = "job";
  private static final long ONE_SEC = 1000L;
  private static final long MAX_WAIT_FOR_USER = 60;
  private static final int CONNECT_TIMEOUT = 500;
  private static final int PAR_RATIO = 5;

  private final Context context;
  private Simulation simulation;
  private CyclicIterator<HttpSpec> scenarioCyclicIterator;
  private Disposable subscribe;
  private volatile boolean shutdownInitiated;

  public ReactiveHttpSimulationRunner(final Context context) {
    this.context = context;
    this.simulation = context.<Simulation>get(JOB).orElseThrow();
    this.scenarioCyclicIterator = new CyclicIterator<>(
        simulation
            .getSpecs()
            .stream()
            .filter(spec -> spec instanceof HttpSpec)
            .map(spec -> (HttpSpec) spec)
            .collect(Collectors.toList()));
  }

  public void start() {

    Out.info("Starting load test for " + simulation.getDuration() + " minutes ...");

    if (SimulationConfig.isGrafanaEnabled()) {
      Out.info("Grafana is enabled. Creating dashboard: " + SimulationConfig.getSimulationId());
      new GrafanaGateway().setUpDashboard(SimulationConfig.getSimulationId(),
          simulation.getSpecs()
              .stream()
              .map(Spec::getName)
              .toArray(String[]::new));
    }

    var userRepository = simulation.getUserRepository();

    // We need to wait till all users are logged in.
    waitUsers(userRepository);

    var httpClientConfig = Dsl.config()
        .setConnectTimeout(CONNECT_TIMEOUT)
        .setMaxConnections(SimulationConfig.getMaxConnections())
        .setKeepAlive(true)
        .build();

    var client = Dsl.asyncHttpClient(httpClientConfig);

    prepareUserSessions(userRepository.getUserSessions());

    this.subscribe = Flux.fromStream(Stream.generate(userRepository::take))
        .take(simulation.getDuration())
        .zipWith(Flux.fromStream(Streams.stream(scenarioCyclicIterator)))
        .doOnTerminate(this::notifyAwaiting)
        .flatMap(tuple2 -> Mono.fromFuture(client.executeRequest(
            getRequestBuilder(
                tuple2.getT2(),
                tuple2.getT1()),
            new HttpSpecAsyncHandler(
                tuple2.getT1().getUser().getId(),
                tuple2.getT2().getEnclosingSpec(),
                tuple2.getT2().getStepName(), simulation))
            .toCompletableFuture()))
        .subscribe();
    await();
    stop();
  }

  private RequestBuilder getRequestBuilder(final HttpSpec httpSpec, final UserSession userSession) {

    RequestBuilder builder = null;
    switch (httpSpec.getMethod()) {
      case GET  : builder = get(httpSpec.getTarget());  break;
      case HEAD : builder = head(httpSpec.getTarget()); break;
      // case X : rest of methods, we support...
      default: throw new NotImplementedException("Not implemented: " + httpSpec.getMethod());
    }

    var user = userSession.getUser();
    if (user instanceof OAuthUser) {
      var token = ((OAuthUser) user).getAccessToken();
      builder = builder.addHeader("Authorization", "Bearer " + token);
    }

    return builder;
  }

  private void await() {
    synchronized (this) {
      try {
        while (!subscribe.isDisposed()) {
          wait(ONE_SEC);
        }
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
        // Intentionally left empty.
      }
    }
  }

  private void notifyAwaiting() {
    synchronized (this) {
      notifyAll();
    }
  }

  @Override
  public void stop() {

    Out.info("Someone pushed the stop() button on runner.");
    shutdown();
  }

  private void shutdown() {
    if (shutdownInitiated) {
      return;
    }

    shutdownInitiated = true;

    Out.info("Stopping the simulation...");

    subscribe.dispose();
    // run cleanup.
    Out.info("Cleaning up.");
    final UserRepository<UserSession> userRepository = simulation.getUserRepository();
    cleanupUserSessions(userRepository.getUserSessions());

    // proceed with shutdown.
    Out.info("Shutting down the system ...");
    scenarioCyclicIterator.stop();
    simulation.stop();

    Out.info("Shutting down completed ...");
    Out.info("Bye!");
  }

  private void waitForASec() {
    Out.info("Wait ...");
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
      Out.info(
          "? Not sufficient user has been logged in. Required " + simulation.getInjectUser() + ". "
              + "Waiting...");
      waitForASec();
    }

    if (!userRepository.has(simulation.getInjectUser())) {
      Out.info(
          "? Not sufficient user in user repository found to be able to run the " + "in "
              + "similation. Check your user source, or reduce the number of max. user the simulation requires "
              + "@Simulation annotation. Required "
              + simulation.getInjectUser() + " user.");

      shutdown();
      System.exit(-1);
    }

    Out.info("User login completed. Total user: " + simulation.getInjectUser());
  }
}
