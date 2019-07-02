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
import io.ryos.rhino.sdk.dsl.HttpSpecMaterializer;
import io.ryos.rhino.sdk.dsl.SomeSpecMaterializer;
import io.ryos.rhino.sdk.dsl.WaitSpecMaterializer;
import io.ryos.rhino.sdk.exceptions.MaterializerNotFound;
import io.ryos.rhino.sdk.io.Out;
import io.ryos.rhino.sdk.monitoring.GrafanaGateway;
import io.ryos.rhino.sdk.specs.ConditionalSpecWrapper;
import io.ryos.rhino.sdk.specs.HttpSpec;
import io.ryos.rhino.sdk.specs.SomeSpec;
import io.ryos.rhino.sdk.specs.Spec;
import io.ryos.rhino.sdk.specs.WaitSpec;
import io.ryos.rhino.sdk.users.repositories.CyclicUserSessionRepositoryImpl;
import io.ryos.rhino.sdk.utils.ReflectionUtils;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactiveHttpSimulationRunner implements SimulationRunner {

  private static final String JOB = "job";
  private static final long ONE_SEC = 1000L;
  private static final long MAX_WAIT_FOR_USER = 60;
  private static final int CONNECT_TIMEOUT = 500;

  private final Context context;
  private SimulationMetadata simulationMetadata;
  private CyclicIterator<ConnectableDsl> dslIterator;
  private Disposable subscribe;
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

    if (SimulationConfig.isGrafanaEnabled()) {
      setUpGrafanaDashboard();
    }

    var userRepository = simulationMetadata.getUserRepository();
    var userSessionProvider =  new CyclicUserSessionRepositoryImpl(userRepository, "all");
    var httpClientConfig = Dsl.config()
        .setConnectTimeout(CONNECT_TIMEOUT)
        .setMaxConnections(SimulationConfig.getMaxConnections())
        .setKeepAlive(true)
        .build();

    var client = Dsl.asyncHttpClient(httpClientConfig);
    var injector = new ReactiveRunnerSimulationInjector(simulationMetadata, null);

    injector.injectOn(simulationMetadata.getTestInstance());

    prepareUserSessions();

    var flux = Flux.fromStream(Stream.generate(userSessionProvider::take));
    var throttlingInfo = simulationMetadata.getThrottlingInfo();

    if (throttlingInfo != null) {
      var rpsLimit = Throttler.Limit.of(throttlingInfo.getNumberOfRequests(),
          throttlingInfo.getDuration());
      flux = flux.transform(throttle(rpsLimit));
    }

    var rampUpInfo = simulationMetadata.getRampUpInfo();
    if (rampUpInfo != null) {
      flux = flux.transform(Rampup.rampup(rampUpInfo.getStartRps(), rampUpInfo.getTargetRps(),
          rampUpInfo.getDuration()));
    }

    this.subscribe = flux.take(simulationMetadata.getDuration())
        .zipWith(Flux.fromStream(stream(dslIterator)))
        .onErrorResume(t -> Mono.empty())
        .doOnError(t -> Out.error(t.getMessage()))
        .doOnTerminate(this::terminate)
        .doOnComplete(() -> shutdownInitiated = true)
        .flatMap(tuple -> {
          var session = tuple.getT1();
          var dsl = tuple.getT2();
          var specIt = dsl.getSpecs().iterator();
          if (!specIt.hasNext()) {
            throw new RuntimeException("No spec found in DSL.");
          }
          var acc = materialize(specIt.next(), client, session);
          while (specIt.hasNext()) {
            // Never move the following statement into lambda body. next() call is required to be eager.
            var next = specIt.next();
            acc = acc.flatMap(s -> {
              if (next instanceof ConditionalSpecWrapper) {
                var predicate = ((ConditionalSpecWrapper) next).getPredicate();
                if (!predicate.test(s)) {
                  return Mono.just(s);
                }
              }
              return materialize(next, client, session);
            });
          }
          return acc.doOnError(System.out::println);
        })
        .subscribe();
    await();
    stop();
  }

  private void terminate() {
    cleanupUserSessions();

    notifyAwaiting();
  }

  private void setUpGrafanaDashboard() {
    Out.info("Grafana is enabled. Creating dashboard: " + SimulationConfig.getSimulationId());
    var grafanaGateway = new GrafanaGateway();
    grafanaGateway.setUpDashboard(SimulationConfig.getSimulationId(),
        simulationMetadata.getSpecs()
            .stream()
            .map(dsl -> (ConnectableDsl) dsl)
            .map(ConnectableDsl::getName)
            .toArray(String[]::new));
  }

  private Mono<UserSession> materialize(final Spec spec, final AsyncHttpClient client,
      final UserSession session) {

    if (spec instanceof HttpSpec) {
      return new HttpSpecMaterializer(client, eventDispatcher)
          .materialize((HttpSpec) spec, session);
    } else if (spec instanceof SomeSpec) {
      return new SomeSpecMaterializer(eventDispatcher).materialize((SomeSpec) spec, session);
    } else if (spec instanceof WaitSpec) {
      return new WaitSpecMaterializer().materialize((WaitSpec) spec, session);
    } else if (spec instanceof ConditionalSpecWrapper) {
      return materialize(((ConditionalSpecWrapper) spec).getSpec(), client, session);
    }

    throw new MaterializerNotFound("Materializer not found for spec: " + spec.getClass().getName());
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

    subscribe.dispose();

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
