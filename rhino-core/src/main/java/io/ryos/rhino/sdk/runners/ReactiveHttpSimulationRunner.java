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

import com.google.common.collect.Streams;
import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.data.Context;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.SpecMaterializer;
import io.ryos.rhino.sdk.io.Out;
import io.ryos.rhino.sdk.monitoring.GrafanaGateway;
import io.ryos.rhino.sdk.specs.Spec;
import io.ryos.rhino.sdk.users.repositories.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.asynchttpclient.Dsl;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public class ReactiveHttpSimulationRunner implements SimulationRunner {

  private static final String JOB = "job";
  private static final long ONE_SEC = 1000L;
  private static final long MAX_WAIT_FOR_USER = 60;
  private static final int CONNECT_TIMEOUT = 500;

  private final Context context;
  private SimulationMetadata simulationMetadata;
  private CyclicIterator<LoadDsl> dslIterator;
  private Disposable subscribe;
  private volatile boolean shutdownInitiated;

  public ReactiveHttpSimulationRunner(final Context context) {
    this.context = context;
    this.simulationMetadata = context.<SimulationMetadata>get(JOB).orElseThrow();
    this.dslIterator = new CyclicIterator<>(
        simulationMetadata
            .getSpecs()
            .stream()
            .filter(spec -> spec instanceof LoadDsl)
            .map(spec -> (LoadDsl) spec)
            .collect(Collectors.toList()));
  }

  public void start() {

    Out.info(
        "Starting load test for " + simulationMetadata.getDuration().toMinutes() + " minutes ...");

    if (SimulationConfig.isGrafanaEnabled()) {
      Out.info("Grafana is enabled. Creating dashboard: " + SimulationConfig.getSimulationId());
      new GrafanaGateway().setUpDashboard(SimulationConfig.getSimulationId(),
          simulationMetadata.getSpecs()
              .stream()
              .map(Spec::getName)
              .toArray(String[]::new));
    }

    var userRepository = simulationMetadata.getUserRepository();

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
        .take(simulationMetadata.getDuration())
        .zipWith(Flux.fromStream(Streams.stream(dslIterator)))
        .doOnTerminate(this::notifyAwaiting)
        .doOnComplete(() -> shutdownInitiated = true)
        .flatMap(tuple -> {
          var session = tuple.getT1();
          var dsl = tuple.getT2();
          return new SpecMaterializer(client).materialize(dsl.specs(), session, simulationMetadata);
        })
        .subscribe();
    await();
    stop();
  }

  private void await() {
    synchronized (this) {
      try {
        while (!shutdownInitiated) {
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
    final UserRepository<UserSession> userRepository = simulationMetadata.getUserRepository();
    cleanupUserSessions(userRepository.getUserSessions());

    // proceed with shutdown.
    Out.info("Shutting down the system ...");
    EventDispatcher.instance(simulationMetadata).stop();
    dslIterator.stop();

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
    userSessions.forEach(us -> simulationMetadata.prepare(us));
  }

  private void cleanupUserSessions(final List<UserSession> userSessions) {
    userSessions.forEach(us -> simulationMetadata.cleanUp(us));
  }

  private void waitUsers(final UserRepository userRepository) {
    Objects.requireNonNull(userRepository);

    int retry = 0;
    while (!userRepository.has(simulationMetadata.getNumberOfUsers())
        && ++retry < MAX_WAIT_FOR_USER) {
      Out.info(
          "? Not sufficient user has been logged in. Required " + simulationMetadata
              .getNumberOfUsers() + ". "
              + "Waiting...");
      waitForASec();
    }

    if (!userRepository.has(simulationMetadata.getNumberOfUsers())) {
      Out.info(
          "? Not sufficient user in user repository found to be able to run the " + "in "
              + "similation. Check your user source, or reduce the number of max. user the simulation requires "
              + "@Simulation annotation. Required "
              + simulationMetadata.getNumberOfUsers() + " user.");

      shutdown();
      System.exit(-1);
    }

    Out.info("User login completed. Total user: " + simulationMetadata.getNumberOfUsers());
  }
}
