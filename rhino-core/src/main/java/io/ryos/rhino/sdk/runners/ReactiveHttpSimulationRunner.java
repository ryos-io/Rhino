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
import static io.ryos.rhino.sdk.utils.ReflectionUtils.executeStaticMethod;

import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.data.Context;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.RunnableDslImpl;
import io.ryos.rhino.sdk.dsl.mat.MaterializerFactory;
import io.ryos.rhino.sdk.dsl.specs.Spec;
import io.ryos.rhino.sdk.dsl.specs.impl.ConditionalSpecWrapper;
import io.ryos.rhino.sdk.exceptions.NoSpecDefinedException;
import io.ryos.rhino.sdk.exceptions.TerminateSimulationException;
import io.ryos.rhino.sdk.users.repositories.CyclicUserSessionRepositoryImpl;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
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
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

public class ReactiveHttpSimulationRunner extends AbstractSimulationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(ReactiveHttpSimulationRunner.class);
  private static final String JOB = "job";
  private CyclicIterator<RunnableDslImpl> dslIterator;
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

    this.dslIterator = new CyclicIterator<>(getSimulationMetadata().getDsls()
        .stream()
        .filter(Objects::nonNull)
        .map(spec -> (RunnableDslImpl) spec)
        .collect(Collectors.toList()));
    this.eventDispatcher = new EventDispatcher(getSimulationMetadata());
    this.masterLock = new ReentrantLock();
    this.continueCondition = masterLock.newCondition();
  }

  public void start() {

    Hooks.onErrorDropped((t) -> {
    });
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
    var injector = new ReactiveRunnerSimulationInjector(simulationMetadata);
    var userList = userSessionProvider.getUserList();

    injector.injectOn(simulationMetadata.getTestInstance());

    prepare(client, userList);

    var flux = Flux.fromStream(Stream.generate(userSessionProvider::take));

    flux = appendRampUp(flux);
    flux = appendThrottling(flux);
    flux = appendTake(flux);
    flux = flux.zipWith(Flux.fromStream(stream(dslIterator)))
        .flatMap(tuple -> getPublisher(client, tuple.getT1(), tuple.getT2()))
        .onErrorResume(this::handleThrowable)
        .doOnError(t -> LOG.error("Something unexpected happened", t))
        .doOnTerminate(this::shutdown)
        .doOnComplete(() -> signalCompletion(() -> this.isPipelineCompleted = true));

    this.subscribe = flux.subscribe();

    awaitIf(() -> !isPipelineCompleted);

    cleanup(client, userList);

    shutdown();
  }

  private void cleanup(AsyncHttpClient client, List<UserSession> userList) {
    if (getSimulationMetadata().getCleanupMethod() != null) {
      LOG.info("Clean-up started.");
      cleanUpUserSessions(userList, client);
      awaitIf(() -> !isCleanupCompleted);
    }
  }

  private void prepare(AsyncHttpClient client, List<UserSession> userList) {
    if (getSimulationMetadata().getPrepareMethod() != null) {
      LOG.info("Preparation started.");
      prepareUserSessions(userList, client);
      awaitIf(() -> !isPrepareCompleted);
    }
  }

  private void awaitIf(Supplier<Boolean> supplier) {
    try {
      masterLock.lock();
      while (supplier.get()) {
        continueCondition.await(1, TimeUnit.SECONDS);
      }
    } catch (InterruptedException e) {
      LOG.error("Interrupted.", e);
      Thread.currentThread().interrupt();
    } finally {
      masterLock.unlock();
    }
  }

  private Publisher<UserSession> getPublisher(AsyncHttpClient client,
      UserSession session, RunnableDslImpl dsl) {

    var specIt = dsl.getSpecs().iterator();
    var materializerFactory = new MaterializerFactory(client, eventDispatcher);

    if (!specIt.hasNext()) {
      throw new NoSpecDefinedException(dsl.getName());
    }

    var acc = materializerFactory.monoFrom(specIt.next(), session);
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
        return materializerFactory.monoFrom(next, session);
      });
    }

    return acc.onErrorResume(exception -> {
      LOG.error(exception.getMessage());
      if (exception instanceof TerminateSimulationException) {
        return Mono.error(exception);
      }
          return Mono.empty();
    });
  }

  private boolean isConditionalSpec(Spec next) {
    return next instanceof ConditionalSpecWrapper;
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
          .flatMap(session -> getPublisher(client, session, executeStaticMethod(method)))
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
      continueCondition.signalAll();
    } finally {
      masterLock.unlock();
    }
  }

  private Publisher<? extends UserSession> handleThrowable(Throwable throwable) {
    LOG.error("Skipping error. Pipeline continues.", throwable);
    return Mono.empty();
  }
}
