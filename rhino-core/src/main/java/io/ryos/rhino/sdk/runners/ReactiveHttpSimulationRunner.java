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

import static com.google.common.collect.Streams.stream;
import static io.ryos.rhino.sdk.runners.Throttler.throttle;

import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.data.Context;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.ConnectableDsl;
import io.ryos.rhino.sdk.io.Out;
import io.ryos.rhino.sdk.monitoring.GrafanaGateway;
import io.ryos.rhino.sdk.users.repositories.CyclicUserSessionRepositoryImpl;
import io.ryos.rhino.sdk.utils.ReflectionUtils;
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
  private static final int HANDSHAKE_TIMEOUT = 60000;
  private static final int READ_TIMEOUT = 60000;

  private final Context context;
  private SimulationMetadata simulationMetadata;
  private CyclicIterator<ConnectableDsl> dslIterator;
  private volatile boolean shutdownInitiated;
  private final EventDispatcher eventDispatcher;

  public ReactiveHttpSimulationRunner(final Context context) {
    this.context = context;
    this.simulationMetadata = context.<SimulationMetadata>get(JOB).orElseThrow();
    this.dslIterator = new CyclicIterator<>(
        simulationMetadata
            .getSpecs()
            .stream()
            .filter(Objects::nonNull)
            .map(spec -> (ConnectableDsl) spec)
            .collect(Collectors.toList()));
    this.eventDispatcher = new EventDispatcher(simulationMetadata);
  }

  public void start() {

    Out.info("Starting load test for " + simulationMetadata.getDuration().toMinutes() + " minutes ...");

    if (simulationMetadata.getGrafanaInfo() != null) {
      setUpGrafanaDashboard();
    }

    var userRepository = simulationMetadata.getUserRepository();
    var userSessionProvider =  new CyclicUserSessionRepositoryImpl(userRepository, "all");
    var httpClientConfig = Dsl.config()
        .setMaxConnections(SimulationConfig.getMaxConnections())
        .setFollowRedirect(true)
        .setMaxRedirects(2)
        .setKeepAlive(true)
        .setCookieStore(null)
        .setConnectTimeout(CONNECT_TIMEOUT)
        .setHandshakeTimeout(HANDSHAKE_TIMEOUT)
        .setReadTimeout(READ_TIMEOUT)
        .setIoThreadsCount(1)
        .build();

    var client = Dsl.asyncHttpClient(httpClientConfig);
    var injector = new ReactiveRunnerSimulationInjector(simulationMetadata, null);

    injector.injectOn(simulationMetadata.getTestInstance());

    prepareUserSessions();

    var flux = Flux.fromStream(Stream.generate(userSessionProvider::take));
    flux = appendThrottling(flux);
    flux = appendRampUp(flux);
    flux.take(simulationMetadata.getDuration())
        .zipWith(Flux.fromStream(stream(dslIterator)))
        .doOnError(t -> Out.error(t.getMessage()))
        .doOnTerminate(this::terminate)
        .doOnComplete(() -> shutdownInitiated = true)
        .subscribe(new SpecSubscriber(eventDispatcher, client));
    await();
    stop();
  }

  private Flux<UserSession> appendThrottling(Flux<UserSession> flux) {
    var throttlingInfo = simulationMetadata.getThrottlingInfo();
    if (throttlingInfo != null) {
      var rpsLimit = Throttler.Limit.of(throttlingInfo.getNumberOfRequests(),
          throttlingInfo.getDuration());
      flux = flux.transform(throttle(rpsLimit));
    }
    return flux;
  }

  private Flux<UserSession> appendRampUp(Flux<UserSession> flux) {
    var rampUpInfo = simulationMetadata.getRampUpInfo();
    if (rampUpInfo != null) {
      flux = flux.transform(Rampup.rampup(rampUpInfo.getStartRps(), rampUpInfo.getTargetRps(),
          rampUpInfo.getDuration()));
    }
    return flux;
  }

  private void terminate() {
    cleanupUserSessions();

    notifyAwaiting();
  }

  private void setUpGrafanaDashboard() {
    Out.info("Grafana is enabled. Creating dashboard: " + SimulationConfig.getSimulationId());
    var grafanaGateway = new GrafanaGateway(simulationMetadata.getGrafanaInfo());
    grafanaGateway.setUpDashboard(SimulationConfig.getSimulationId(),
        simulationMetadata.getSpecs()
            .stream()
            .map(dsl -> (ConnectableDsl) dsl)
            .map(ConnectableDsl::getName)
            .toArray(String[]::new));
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
    terminate();
  }

  private void shutdown() {
    if (shutdownInitiated) {
      return;
    }

    shutdownInitiated = true;

    Out.info("Stopping the simulation...");

    // proceed with shutdown.
    Out.info("Shutting down the system ...");
    eventDispatcher.stop();
    dslIterator.stop();

    Out.info("Shutting down completed ...");
    Out.info("Bye!");
  }

  private void waitForASec() {
    Out.info("Wait ...");
    try {
      Thread.sleep(ONE_SEC);
    } catch (InterruptedException e) {
      Out.info("Wait-Interrupted.");
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
}
