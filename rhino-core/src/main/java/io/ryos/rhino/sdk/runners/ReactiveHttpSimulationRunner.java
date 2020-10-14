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
import io.ryos.rhino.sdk.HttpClient;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.data.Context;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.DslMethod;
import io.ryos.rhino.sdk.dsl.impl.DslMethodImpl;
import io.ryos.rhino.sdk.dsl.mat.DslMethodMaterializer;
import io.ryos.rhino.sdk.users.repositories.CyclicUserSessionRepositoryImpl;
import java.io.IOException;
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
  private CyclicIterator<DslMethod> dslIterator;
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

  private void execute() {
    execute(null);
  }

  private void execute(final Integer numberOfRepeats) {
    Hooks.onErrorDropped(t -> {
    });
    var simulationMetadata = getSimulationMetadata();

    printStart(numberOfRepeats, simulationMetadata);

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

    flux = appendThrottling(flux);
    flux = appendTake(flux, getRepeats(numberOfRepeats));
    flux = flux.zipWith(Flux.fromStream(stream(dslIterator)))
        .doOnError(t -> LOG.error("Something unexpected happened", t))
        .flatMap(tuple -> tuple.getT2().materializer().materialize(tuple.getT1()))
        .onErrorResume(this::handleThrowable)
        .doOnError(t -> LOG.error("Something unexpected happened", t))
        .doOnTerminate(this::shutdown)
        .doOnComplete(() -> signalCompletion(() -> this.isPipelineCompleted = true));

    this.subscribe = flux.subscribe();

    awaitIf(() -> !isPipelineCompleted);

    cleanup(userList);

    shutdown();
  }

  private void printStart(Integer numberOfRepeats, SimulationMetadata simulationMetadata) {
    if (null != numberOfRepeats && numberOfRepeats == 1) {
      System.out.println("Starting the verification tests.");
    }
    if (null == numberOfRepeats) {
      System.out.println(String.format("Starting the load tests for %s minutes.",
          simulationMetadata.getDuration().toMinutes()));
    }
    if (null != numberOfRepeats && numberOfRepeats > 1) {
      System.out.println(String.format("Starting the performance tests for %s cycles.",
          numberOfRepeats));
    }
  }

  private int getRepeats(final Integer numberOfRepeats) {
    int repeats = getStopAfterFromEnv();
    if (numberOfRepeats != null && repeats < 0) {
      repeats = numberOfRepeats;
    }
    return repeats;
  }

  @Override
  public void start() {
    execute();
  }

  @Override
  public void verify() {
    execute(1);
  }

  @Override
  public void times(int numberOfRepeats) {
    execute(numberOfRepeats);
  }

  private void cleanup(List<UserSession> userList) {
    if (getSimulationMetadata().getCleanupMethod() != null) {
      System.out.println("Clean-up started.");
      executeAfter(userList);
      awaitIf(() -> !isCleanupCompleted);
    } else {
      isCleanupCompleted = true;
    }
  }

  private void prepare(List<UserSession> userList) {
    if (getSimulationMetadata().getBeforeMethod() != null) {
      System.out.println("Preparation started.");
      executeBefore(userList);
      awaitIf(() -> !isPrepareCompleted);
    } else {
      isPrepareCompleted = true;
    }
  }

  private void awaitIf(Supplier<Boolean> supplier) {
    try {
      masterLock.lock();
      while (supplier.get()) {
        continueCondition.await(1, TimeUnit.SECONDS);
      }
    } catch (InterruptedException e) {
      System.err.println("Interrupted." + e.getLocalizedMessage());
      Thread.currentThread().interrupt();
    } finally {
      masterLock.unlock();
    }
  }

  @Override
  public void stop() {
    System.out.println("Simulation completed.");
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

    System.out.println("Stopping the simulation...");
    subscribe.dispose();
    dslIterator.stop();
    EventDispatcher.getInstance().stop();
    try {
      HttpClient.INSTANCE.getClient().close();
    } catch (IOException e) {
      LOG.debug("Error shutting down http client", e);
    }

    System.out.println("Shutting down completed ...");
    System.out.println("Bye!");
  }

  private void executeBefore(List<UserSession> userSessionList) {
    if (getSimulationMetadata().getBeforeMethod() != null) {
      materializeMethod("Before", getSimulationMetadata().getBeforeMethod(),
          userSessionList,
          () -> {
            isPrepareCompleted = true;
            System.out.println("Preparation completed.");
          });
    }
  }

  private void executeAfter(List<UserSession> userSessionList) {
    if (getSimulationMetadata().getAfterMethod() != null) {
      materializeMethod("After", getSimulationMetadata().getAfterMethod(),
          userSessionList,
          () -> {
            System.out.println("Clean-up completed.");
            isCleanupCompleted = true;
          });
    }
  }

  private void materializeMethod(final String callerName, final Method method,
      final List<UserSession> userSessionList, final Action action) {
    if (method != null) {
      DslMethodImpl dslItem = new DslMethodImpl(callerName, executeMethod(method,
          getSimulationMetadata().getTestInstance()));

      Flux.fromStream(userSessionList.stream())
          .onErrorResume(this::handleThrowable)
          .flatMap(session -> new DslMethodMaterializer(dslItem).materialize(session))
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
    System.err.println("Skipping error. Pipeline continues." + throwable);
    return Mono.empty();
  }
}
