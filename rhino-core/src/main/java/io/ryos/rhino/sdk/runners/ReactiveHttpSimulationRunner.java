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
import static io.ryos.rhino.sdk.utils.ReflectionUtils.executeStaticMethod;

import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.data.Context;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.ConnectableDsl;
import io.ryos.rhino.sdk.dsl.HttpSpecMaterializer;
import io.ryos.rhino.sdk.dsl.MapperSpecMaterializer;
import io.ryos.rhino.sdk.dsl.SomeSpecMaterializer;
import io.ryos.rhino.sdk.dsl.WaitSpecMaterializer;
import io.ryos.rhino.sdk.exceptions.MaterializerNotFound;
import io.ryos.rhino.sdk.exceptions.NoSpecDefinedException;
import io.ryos.rhino.sdk.specs.ConditionalSpecWrapper;
import io.ryos.rhino.sdk.specs.HttpSpec;
import io.ryos.rhino.sdk.specs.MapperSpec;
import io.ryos.rhino.sdk.specs.SomeSpec;
import io.ryos.rhino.sdk.specs.Spec;
import io.ryos.rhino.sdk.specs.WaitSpec;
import io.ryos.rhino.sdk.users.repositories.CyclicUserSessionRepositoryImpl;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.filter.ThrottleRequestFilter;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class ReactiveHttpSimulationRunner extends AbstractSimulationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(ReactiveHttpSimulationRunner.class);
  private static final String JOB = "job";

  private final Context context;
  private CyclicIterator<ConnectableDsl> dslIterator;
  private Disposable subscribe;

  private volatile boolean shutdownInitiated;
  private volatile boolean isCleanupCompleted;
  private volatile boolean isPrepareCompleted;
  private volatile boolean isPipelineCompleted;

  private final Condition continueCondition;
  private final Lock masterLock;

  private final EventDispatcher eventDispatcher;

  public ReactiveHttpSimulationRunner(final Context context) {
    super(context.<SimulationMetadata>get(JOB).orElseThrow());
    this.context = context;
    this.dslIterator = new CyclicIterator<>(getSimulationMetadata().getDsls()
        .stream()
        .filter(Objects::nonNull)
        .map(spec -> (ConnectableDsl) spec)
        .collect(Collectors.toList()));
    this.eventDispatcher = new EventDispatcher(getSimulationMetadata());
    this.masterLock = new ReentrantLock();
    this.continueCondition = masterLock.newCondition();
  }

  public void start() {

    var simulationMetadata = getSimulationMetadata();

    LOG.info("Starting load test for {} minutes ...", simulationMetadata.getDuration().toMinutes());

    if (simulationMetadata.getGrafanaInfo() != null) {
      setUpGrafanaDashboard();
    }

    var userRepository = simulationMetadata.getUserRepository();
    var userSessionProvider = new CyclicUserSessionRepositoryImpl(userRepository, "all");
    var httpClientConfig = Dsl.config()
        .setKeepAlive(true)
        .setMaxConnections(SimulationConfig.getMaxConnections())
        .setConnectTimeout(SimulationConfig.getHttpConnectTimeout())
        .setHandshakeTimeout(SimulationConfig.getHttpHandshakeTimeout())
        .setReadTimeout(SimulationConfig.getHttpReadTimeout())
        .setRequestTimeout(SimulationConfig.getHttpRequestTimeout())
        .addRequestFilter(new ThrottleRequestFilter(SimulationConfig.getMaxConnections()))
        .build();

    var client = Dsl.asyncHttpClient(httpClientConfig);
    var injector = new ReactiveRunnerSimulationInjector(simulationMetadata, null);
    var userList = userSessionProvider.getUserList();

    injector.injectOn(simulationMetadata.getTestInstance());

    prepare(client, userList);

    var flux = Flux.fromStream(Stream.generate(userSessionProvider::take));

    flux = appendRampUp(flux);
    flux = appendThrottling(flux);
    flux = flux.take(simulationMetadata.getDuration())
        .zipWith(Flux.fromStream(stream(dslIterator)))
        .onErrorResume(this::handleError)
        .doOnError(t -> LOG.error("Something unexpected happened", t))
        .doOnTerminate(this::shutdown)
        .doOnComplete(() -> signalCompletion(() -> isPrepareCompleted = true))
        .flatMap(tuple -> materializeToPublisher(client, tuple.getT1(), tuple.getT2()));

    this.subscribe = flux.subscribe();

    awaitIf(!isPipelineCompleted);

    cleanup(client, userList);

    shutdown();
  }

  private void cleanup(AsyncHttpClient client, List<UserSession> userList) {
    if (getSimulationMetadata().getCleanupMethod() != null) {
      LOG.info("Clean-up started.");
      cleanUpUserSessions(userList, client);
      awaitIf(!isCleanupCompleted);
    }
  }

  private void prepare(AsyncHttpClient client, List<UserSession> userList) {
    if (getSimulationMetadata().getPrepareMethod() != null) {
      LOG.info("Preparation started.");
      prepareUserSessions(userList, client);
      awaitIf(!isPrepareCompleted);
    }
  }

  private void awaitIf(boolean conditional) {
    try {
      masterLock.lock();
      if (conditional) {
        continueCondition.await();
      }
    } catch (InterruptedException e) {
      LOG.error("Interrupted.", e);
    } finally {
      masterLock.unlock();
    }
  }

  private Publisher<? extends Tuple2<UserSession, ConnectableDsl>> handleError(Throwable t) {
    LOG.error("Skipping error", t);
    return Mono.empty();
  }

  private boolean isConditionalSpec(Spec next) {
    return next instanceof ConditionalSpecWrapper;
  }

  private Flux<UserSession> appendThrottling(Flux<UserSession> flux) {
    var throttlingInfo = getSimulationMetadata().getThrottlingInfo();
    if (throttlingInfo != null) {
      var rpsLimit = Throttler.Limit.of(throttlingInfo.getRps(),
          throttlingInfo.getDuration());
      flux = flux.transform(throttle(rpsLimit));
    }
    return flux;
  }

  private Flux<UserSession> appendRampUp(Flux<UserSession> flux) {
    var rampUpInfo = getSimulationMetadata().getRampUpInfo();
    if (rampUpInfo != null) {
      flux = flux.transform(Rampup.rampup(rampUpInfo.getStartRps(), rampUpInfo.getTargetRps(),
          rampUpInfo.getDuration()));
    }
    return flux;
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
    } else if (spec instanceof MapperSpec) {
      return new MapperSpecMaterializer().materialize((MapperSpec) spec, session);
    } else if (isConditionalSpec(spec)) {
      return materialize(((ConditionalSpecWrapper) spec).getSpec(), client, session);
    }

    throw new MaterializerNotFound("Materializer not found for spec: " + spec.getClass().getName());
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

    if (!isCleanupCompleted) {
      return;
    }

    shutdownInitiated = true;

    LOG.info("Stopping the simulation...");
    subscribe.dispose();
    dslIterator.stop();
    eventDispatcher.stop();

    LOG.info("Shutting down completed ...");
    LOG.info("Bye!");
  }

  private void prepareUserSessions(List<UserSession> userSessionList, AsyncHttpClient client) {
    if (getSimulationMetadata().getPrepareMethod() != null) {
      materializeMethod(getSimulationMetadata().getPrepareMethod(),
          userSessionList, client,
          () -> {
        isPrepareCompleted = true;
        LOG.info("Preparation completed.");
      });
    }
  }

  private void cleanUpUserSessions(List<UserSession> userSessionList, AsyncHttpClient client) {
    if (getSimulationMetadata().getCleanupMethod() != null) {
      materializeMethod(getSimulationMetadata().getCleanupMethod(),
          userSessionList, client,
          () -> {
            LOG.info("Clean-up completed.");
            isCleanupCompleted = true;
            eventDispatcher.stop();
          });
    }
  }

  private void materializeMethod(final Method method, final List<UserSession> userSessionList,
      final AsyncHttpClient client, final Action action) {

    if (method != null) {
      Flux.fromStream(userSessionList.stream())
          .onErrorResume(this::handleThrowable)
          .flatMap(session -> materializeToPublisher(client, session, executeStaticMethod(method, session)))
          .doOnError(throwable -> LOG.error("Something unexpected happened", throwable))
          .doOnComplete(() -> signalCompletion(action))
          .blockLast();
    }
  }

  @FunctionalInterface
  public interface Action {

    void execute();
  }

  private void signalCompletion(Action action) {
    try {
      masterLock.lock();
      action.execute();
      continueCondition.signal();
    } catch (IllegalMonitorStateException e) {
      LOG.debug("Await not called yet. The cleanup completed before the main thread got to"
          + " be awaited. Main thread will continue.");
    } finally {
      masterLock.unlock();
    }
  }

  private Publisher<? extends UserSession> handleThrowable(Throwable throwable) {
    LOG.error("Skipping error. Pipeline continues.", throwable);
    return Mono.empty();
  }

  private Publisher<? extends UserSession> materializeToPublisher(final AsyncHttpClient client,
      final UserSession session,
      final ConnectableDsl dsl) {

    var specIt = dsl.getSpecs().iterator();
    if (!specIt.hasNext()) {
      throw new NoSpecDefinedException(dsl.getName());
    }
    var acc = materialize(specIt.next(), client, session);
    while (specIt.hasNext()) {
      // Never move the following statement into lambda body. next() call is required to be eager.
      var next = specIt.next();
      acc = acc.flatMap(s -> {
        if (isConditionalSpec(next)) {
          var predicate = ((ConditionalSpecWrapper) next).getPredicate();
          if (!predicate.test(s)) {
            return Mono.just(s);
          }
        }
        return materialize(next, client, session);
      });
    }
    return acc.doOnError(e -> LOG.error("Unexpected error: ", e));
  }
}
