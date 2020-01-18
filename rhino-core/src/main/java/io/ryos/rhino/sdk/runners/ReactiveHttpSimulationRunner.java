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
import static io.ryos.rhino.sdk.utils.ReflectionUtils.executeMethod;

import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.data.Context;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.mat.SpecMaterializer;
import io.ryos.rhino.sdk.dsl.specs.DSLItem;
import io.ryos.rhino.sdk.dsl.specs.DSLMethod;
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.Materializable;
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
  private static final String ALL_REGIONS = "all";
  private CyclicIterator<DSLMethod> dslIterator;
  private Disposable subscribe;

  private volatile boolean shutdownInitiated;
  private volatile boolean isCleanupCompleted;
  private volatile boolean isPrepareCompleted;
  private volatile boolean isPipelineCompleted;

  private final Condition continueCondition;
  private final Lock masterLock;

  public ReactiveHttpSimulationRunner(final Context context) {
    super(context.<SimulationMetadata>get(JOB).orElseThrow());

    this.dslIterator = new CyclicIterator<>(getSimulationMetadata().getDslMethods()
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toList()));
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
    var userSessionProvider = new CyclicUserSessionRepositoryImpl(userRepository, ALL_REGIONS);
    var injector = new ReactiveRunnerSimulationInjector(simulationMetadata);
    var userList = userSessionProvider.getUserList();

    injector.injectOn(simulationMetadata.getTestInstance());

    prepare(userList);

    var flux = Flux.fromStream(Stream.generate(userSessionProvider::take));

    flux = appendRampUp(flux);
    flux = appendThrottling(flux);
    flux = appendTake(flux);
    flux = flux.zipWith(Flux.fromStream(stream(dslIterator)))
        .flatMap(tuple -> materializeDSLMethod(tuple.getT1(), tuple.getT2()))
        .onErrorResume(this::handleThrowable)
        .doOnError(t -> LOG.error("Something unexpected happened", t))
        .doOnTerminate(this::shutdown)
        .doOnComplete(() -> signalCompletion(() -> this.isPipelineCompleted = true));

    this.subscribe = flux.subscribe();

    awaitIf(() -> !isPipelineCompleted);

    cleanup(userList);

    shutdown();
  }

  private void cleanup(List<UserSession> userList) {
    if (getSimulationMetadata().getCleanupMethod() != null) {
      LOG.info("Clean-up started.");
      cleanUpUserSessions(userList);
      awaitIf(() -> !isCleanupCompleted);
    }
  }

  private void prepare(List<UserSession> userList) {
    if (getSimulationMetadata().getBeforeMethod() != null) {
      LOG.info("Preparation started.");
      prepareUserSessions(userList);
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

  private Publisher<UserSession> materializeDSLMethod(final UserSession session,
      final DSLItem dsl) {
    var childrenIterator = dsl.getChildren().iterator();
    if (!childrenIterator.hasNext()) {
      throw new NoSpecDefinedException(dsl.getName());
    }

    var nextItem = childrenIterator.next();
    nextItem.setParent(dsl);
    var materializer = createDSLSpecMaterializer(session, nextItem);
    var acc = materializer.materialize((DSLSpec) nextItem, session);

    while (childrenIterator.hasNext()) {
      var next = childrenIterator.next();
      acc = acc.flatMap(s -> {
        if (isConditionalSpec((DSLSpec) next)) {
          var predicate = ((ConditionalSpecWrapper) next).getPredicate();
          if (!predicate.test(s)) {
            return Mono.just(s);
          }
        }
        return createDSLSpecMaterializer(session, next).materialize((DSLSpec) next, session);
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

  private SpecMaterializer<DSLSpec> createDSLSpecMaterializer(UserSession session,
      DSLItem nextItem) {
    SpecMaterializer<DSLSpec> materializer;
    if (nextItem instanceof Materializable) {
      materializer = ((Materializable) nextItem).createMaterializer(session);
    } else {
      throw new RuntimeException("DSLItem is not materializable.");
    }
    return materializer;
  }

  private boolean isConditionalSpec(DSLSpec next) {
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

    LOG.info("Shutting down completed ...");
    LOG.info("Bye!");
  }

  private void prepareUserSessions(List<UserSession> userSessionList) {
    if (getSimulationMetadata().getBeforeMethod() != null) {
      materializeMethod(getSimulationMetadata().getBeforeMethod(),
          userSessionList,
          () -> {
            isPrepareCompleted = true;
            LOG.info("Preparation completed.");
          });
    }
  }

  private void cleanUpUserSessions(List<UserSession> userSessionList) {
    if (getSimulationMetadata().getAfterMethod() != null) {
      materializeMethod(getSimulationMetadata().getAfterMethod(),
          userSessionList,
          () -> {
            LOG.info("Clean-up completed.");
            isCleanupCompleted = true;
          });
    }
  }

  private void materializeMethod(final Method method, final List<UserSession> userSessionList,
      final Action action) {
    if (method != null) {
      Flux.fromStream(userSessionList.stream())
          .onErrorResume(this::handleThrowable)
          .flatMap(session -> {
            DSLItem runnable = executeMethod(method, getSimulationMetadata().getTestInstance());
            return materializeDSLMethod(session, runnable);
          })
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
