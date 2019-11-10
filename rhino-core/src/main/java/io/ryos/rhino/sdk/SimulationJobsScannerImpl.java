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

package io.ryos.rhino.sdk;

import static io.ryos.rhino.sdk.utils.ReflectionUtils.enhanceInstanceAt;
import static io.ryos.rhino.sdk.utils.ReflectionUtils.getFieldsByAnnotation;
import static io.ryos.rhino.sdk.utils.ReflectionUtils.instanceOf;
import static io.ryos.rhino.sdk.utils.ReflectionUtils.setValueAtInjectionPoint;
import static java.util.stream.Collectors.toList;

import io.ryos.rhino.sdk.annotations.After;
import io.ryos.rhino.sdk.annotations.Before;
import io.ryos.rhino.sdk.annotations.CleanUp;
import io.ryos.rhino.sdk.annotations.Disabled;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Grafana;
import io.ryos.rhino.sdk.annotations.Influx;
import io.ryos.rhino.sdk.annotations.Logging;
import io.ryos.rhino.sdk.annotations.Prepare;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.Throttle;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.data.Pair;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.SimulationSession;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.RunnableDslImpl;
import io.ryos.rhino.sdk.exceptions.IllegalMethodSignatureException;
import io.ryos.rhino.sdk.exceptions.RepositoryNotFoundException;
import io.ryos.rhino.sdk.exceptions.RhinoFrameworkError;
import io.ryos.rhino.sdk.exceptions.ScenarioNotFoundException;
import io.ryos.rhino.sdk.exceptions.SpecificationNotFoundException;
import io.ryos.rhino.sdk.runners.DefaultSimulationRunner;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.users.repositories.DefaultUserRepositoryFactoryImpl;
import io.ryos.rhino.sdk.users.repositories.UserRepository;
import io.ryos.rhino.sdk.users.repositories.UserRepositoryFactory;
import io.ryos.rhino.sdk.utils.ReflectionUtils;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimulationJobsScannerImpl implements SimulationJobsScanner {

  private static final Logger LOG = LogManager.getLogger(SimulationJobsScannerImpl.class);
  private static final String DOT = ".";

  public SimulationMetadata createBenchmarkJob(final Class clazz) {

    // Simulation class.
    var simAnnotation = (io.ryos.rhino.sdk.annotations.Simulation) clazz
        .getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.Simulation.class);

    // User repository annotation.
    var repoAnnotation = Optional.ofNullable((io.ryos.rhino.sdk.annotations.UserRepository) clazz
        .getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.UserRepository.class));

    var runnerAnnotation = (io.ryos.rhino.sdk.annotations.Runner) clazz
        .getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.Runner.class);

    var rampupInfo = getRampupInfo(clazz, simAnnotation);
    var throttlingInfo = getThrottlingInfo(clazz, simAnnotation);
    var enableInflux = clazz.getDeclaredAnnotation(Influx.class) != null;
    var grafanaInfo = getGrafanaInfo(clazz);

    // Read scenario methods.
    var scenarioMethods = Arrays.stream(clazz.getDeclaredMethods())
        .filter(this::hasScenarioAnnotation)
        .filter(this::isEnabled)
        .map(method -> new Scenario(getScenarioName(method), method))
        .collect(toList());

    // Create test instance.
    var testInstance = instanceOf(clazz).orElseThrow();
    if (isReactiveSimulation(runnerAnnotation)) {
      enhanceInjectionPoints(clazz, testInstance);
    }

    var dsls = Arrays.stream(clazz.getDeclaredMethods())
        .filter(this::hasDslAnnotation)
        .filter(this::isEnabled)
        .map(method -> new Pair<>(method.getDeclaredAnnotation(Dsl.class).name(),
            ReflectionUtils.<LoadDsl>executeMethod(method, testInstance)))
        .map(this::getLoadDsl)
        .collect(toList());

    if (scenarioMethods.isEmpty() && isBlockingSimulation(runnerAnnotation)) {
      throw new ScenarioNotFoundException(clazz.getName());
    }

    if (dsls.isEmpty() && isReactiveSimulation(runnerAnnotation)) {
      throw new SpecificationNotFoundException(clazz.getName());
    }

    // Gather logging information from annotation.
    var loggingAnnotation = (Logging) clazz.getDeclaredAnnotation(Logging.class);
    var logger = Optional.ofNullable(loggingAnnotation).map(Logging::file).orElse(null);
    var userRepo = repoAnnotation.map(this::createUserRepository)
        .orElse(new DefaultUserRepositoryFactoryImpl().create());

    Method prepareMethod = null;
    Method cleanupMethod = null;

    if (isReactiveSimulation(runnerAnnotation)) {
      prepareMethod = findStaticMethodWith(clazz, Prepare.class).orElse(null);
      cleanupMethod = findStaticMethodWith(clazz, CleanUp.class).orElse(null);
    } else if (isBlockingSimulation(runnerAnnotation)) {
      prepareMethod = findStaticMethodWith(clazz, Prepare.class, SimulationSession.class)
          .orElse(null);
      cleanupMethod = findStaticMethodWith(clazz, CleanUp.class, SimulationSession.class)
          .orElse(null);
    }

    return new SimulationMetadata.Builder()
        .withSimulationClass(clazz)
        .withUserRepository(userRepo)
        .withRunner(
            runnerAnnotation != null ? runnerAnnotation.clazz()
                : ReactiveHttpSimulationRunner.class)
        .withSimulation(simAnnotation.name())
        .withDuration(Duration.ofMinutes(simAnnotation.durationInMins()))
        .withUserRegion(simAnnotation.userRegion())
        .withInjectUser(simAnnotation.maxNumberOfUsers())
        .withLogWriter(validateLogFile(logger))
        .withInflux(enableInflux)
        .withPrepare(prepareMethod)
        .withCleanUp(cleanupMethod)
        .withBefore(findMethodWith(clazz, Before.class).orElse(null))
        .withAfter(findMethodWith(clazz, After.class).orElse(null))
        .withScenarios(scenarioMethods)
        .withDsls(dsls)
        .withTestInstance(testInstance)
        .withThrottling(throttlingInfo)
        .withRampUp(rampupInfo)
        .withGrafana(grafanaInfo)
        .build();
  }

  private LoadDsl getLoadDsl(Pair<String, LoadDsl> pair) {
    var loadDsl = pair.getSecond();
    if (loadDsl instanceof RunnableDslImpl) {
      return ((RunnableDslImpl) loadDsl).withName(pair.getFirst());
    }
    return loadDsl;
  }

  private GrafanaInfo getGrafanaInfo(Class clazz) {
    Grafana grafanaAnnotation = (Grafana) clazz.<Grafana>getDeclaredAnnotation(Grafana.class);
    if (grafanaAnnotation != null) {
      return new GrafanaInfo(grafanaAnnotation.dashboard(), grafanaAnnotation.name());
    }
    return null;
  }

  private ThrottlingInfo getThrottlingInfo(Class clazz, Simulation simAnnotation) {
    var throttlingAnnotation = (Throttle) clazz.getDeclaredAnnotation(Throttle.class);
    if (throttlingAnnotation != null) {
      var duration = simAnnotation.durationInMins();
      if (throttlingAnnotation.durationInMins() >= 0) {
        duration = throttlingAnnotation.durationInMins();
      }
      return new ThrottlingInfo(throttlingAnnotation.rps(), Duration.ofMinutes(duration));
    }
    return null;
  }

  private RampupInfo getRampupInfo(Class clazz, Simulation simAnnotation) {
    var rampUpAnnotation = (io.ryos.rhino.sdk.annotations.RampUp) clazz
        .getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.RampUp.class);
    if (rampUpAnnotation != null) {
      var duration = simAnnotation.durationInMins();
      if (rampUpAnnotation.durationInMins() >= 0) {
        duration = rampUpAnnotation.durationInMins();
      }
      return new RampupInfo(rampUpAnnotation.startRps(), rampUpAnnotation.targetRps(),
          Duration.ofMinutes(duration));
    }
    return null;
  }

  private void enhanceInjectionPoints(Class simClass, Object instance) {
    var userProviders = getFieldsByAnnotation(simClass, UserProvider.class);
    userProviders
        .forEach(p -> setValueAtInjectionPoint(enhanceInstanceAt(p.getFirst()), p.getFirst(),
            instance));

    var providers = getFieldsByAnnotation(simClass, Provider.class);
    providers.forEach(p -> setValueAtInjectionPoint(enhanceInstanceAt(p.getFirst()),
        p.getFirst(), instance));
  }

  private boolean hasDslAnnotation(Method method) {
    return Arrays.stream(method.getDeclaredAnnotations()).anyMatch(a -> a instanceof Dsl);
  }

  private boolean hasScenarioAnnotation(Method method) {
    return Arrays.stream(method.getDeclaredAnnotations())
        .anyMatch(annotation -> annotation instanceof io.ryos.rhino.sdk.annotations.Scenario);
  }

  private boolean isEnabled(Method method) {
    return method.getDeclaredAnnotation(Disabled.class) == null;
  }

  private String getScenarioName(Method method) {
    return method.getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.Scenario.class).name();
  }

  private boolean isBlockingSimulation(final Runner runnerAnnotation) {
    return runnerAnnotation == null || runnerAnnotation.clazz()
        .equals(DefaultSimulationRunner.class);
  }

  private boolean isReactiveSimulation(final Runner runnerAnnotation) {
    return runnerAnnotation != null && runnerAnnotation.clazz()
        .equals(ReactiveHttpSimulationRunner.class);
  }

  private String validateLogFile(final String logFile) {
    if (logFile == null) {
      return null;
    }

    var simFile = new File(logFile);

    try {
      var newFile = simFile.createNewFile();
      if (!newFile && !simFile.canWrite()) {
        throw new IOException(
            "Not sufficient permissions to write the simulation file: " + simFile);
      }
    } catch (IOException e) {
      LOG.error("! Simulation log file is invalid: {}", simFile);
      System.exit(-1);
    }
    return logFile;
  }

  private UserRepository createUserRepository(
      final io.ryos.rhino.sdk.annotations.UserRepository userRepository) {

    var factory = userRepository.factory();
    var loginDelay = userRepository.delay();

    try {
      final Constructor<? extends UserRepositoryFactory> factoryConstructor =
          factory.getConstructor(long.class);
      final UserRepositoryFactory userRepositoryFactory =
          factoryConstructor.newInstance(loginDelay);
      return userRepositoryFactory.create();
    } catch (NoSuchMethodException nsme) {
      return createWithDefaultConstructor(factory);
    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
      LOG.error(e);
    }

    throw new RepositoryNotFoundException();
  }

  private UserRepository createWithDefaultConstructor(
      Class<? extends UserRepositoryFactory> factory) {
    try {
      return factory.getDeclaredConstructor().newInstance().create();
    } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      LOG.error(e);
      throw new RhinoFrameworkError("Cannot create object instance.", e);
    }
  }

  private <T extends Annotation> Optional<Method> findMethodWith(Class<?> clazz,
      Class<T> annotation) {
    return Arrays.stream(clazz.getDeclaredMethods()).
        filter(m -> Arrays.stream(m.getDeclaredAnnotations()).
            anyMatch(annotation::isInstance)).
        findFirst(); // TODO only the first step method?
  }

  private <T extends Annotation> Optional<Method> findStaticMethodWith(Class<?> clazz,
      Class<T> annotation, Class<?>... args) {

    return Arrays.stream(clazz.getMethods()).
        filter(m -> Arrays.stream(m.getAnnotations()).
            anyMatch(annotation::isInstance)).
        findFirst()
        .map(Method::getName)
        .map(name -> getStaticMethod(clazz, name, args));
  }

  private Method getStaticMethod(Class<?> clazz, String name, Class<?>... args) {
    try {
      return clazz.getMethod(name, args);
    } catch (NoSuchMethodException e) {
      throw new IllegalMethodSignatureException(
          "Static method with name: " + name + "on class:" + clazz.toString());
    }
  }
}
