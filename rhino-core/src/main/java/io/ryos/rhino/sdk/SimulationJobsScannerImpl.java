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
import io.ryos.rhino.sdk.annotations.Disabled;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Grafana;
import io.ryos.rhino.sdk.annotations.Influx;
import io.ryos.rhino.sdk.annotations.Logging;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.data.Pair;
import io.ryos.rhino.sdk.dsl.DslBuilder;
import io.ryos.rhino.sdk.dsl.DslMethod;
import io.ryos.rhino.sdk.dsl.impl.DslBuilderImpl;
import io.ryos.rhino.sdk.dsl.impl.DslMethodImpl;
import io.ryos.rhino.sdk.exceptions.RepositoryNotFoundException;
import io.ryos.rhino.sdk.exceptions.RhinoFrameworkError;
import io.ryos.rhino.sdk.exceptions.SpecificationNotFoundException;
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
import java.util.Arrays;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimulationJobsScannerImpl implements SimulationJobsScanner {

  private static final Logger LOG = LogManager.getLogger(SimulationJobsScannerImpl.class);

  public SimulationMetadata createBenchmarkJob(final Class clazz) {
    var testInstance = instanceOf(clazz).orElseThrow();
    var simAnnotation = (io.ryos.rhino.sdk.annotations.Simulation) clazz
        .getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.Simulation.class);
    var repoAnnotation = Optional.ofNullable((io.ryos.rhino.sdk.annotations.UserRepository) clazz
        .getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.UserRepository.class));
    var runnerAnnotation = (io.ryos.rhino.sdk.annotations.Runner) clazz
        .getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.Runner.class);
    var enableInflux = clazz.getDeclaredAnnotation(Influx.class) != null;
    var grafanaInfo = getGrafanaInfo(clazz);

    // Create test instance.
    enhanceInjectionPoints(clazz, testInstance);

    var dslMethods = Arrays.stream(clazz.getDeclaredMethods())
        .filter(this::hasDslAnnotation)
        .filter(this::isEnabled)
        .map(method -> createDslMethod(testInstance, method))
        .collect(toList());
    var dsls = Arrays.stream(clazz.getDeclaredMethods())
        .filter(this::hasDslAnnotation)
        .filter(this::isEnabled)
        .map(method -> new Pair<>(method.getDeclaredAnnotation(Dsl.class).name(),
            ReflectionUtils.<DslBuilder>executeMethod(method, testInstance)))
        .map(this::getLoadDsl)
        .collect(toList());

    if (dsls.isEmpty()) {
      throw new SpecificationNotFoundException("No dsl method with @Dsl annotation found in the simulation class: " + clazz.getName());
    }

    // Gather logging information from annotation.
    var loggingAnnotation = (Logging) clazz.getDeclaredAnnotation(Logging.class);
    var logger = Optional.ofNullable(loggingAnnotation).map(Logging::file).orElse(null);
    var userRepo = repoAnnotation.map(this::createUserRepository)
        .orElse(new DefaultUserRepositoryFactoryImpl().create());

    return new SimulationMetadata.Builder()
        .withSimulationClass(clazz)
        .withUserRepository(userRepo)
        .withRunner(runnerAnnotation != null
            ? runnerAnnotation.clazz()
            : ReactiveHttpSimulationRunner.class)
        .withSimulation(simAnnotation.name())
        .withDuration(SimulationConfig.getDuration())
        .withUserRegion(simAnnotation.userRegion())
        .withInjectUser(SimulationConfig.getMaxNumberOfUsers())
        .withLogWriter(validateLogFile(logger))
        .withInflux(enableInflux)
        .withBefore(findMethodWith(clazz, Before.class).orElse(null))
        .withAfter(findMethodWith(clazz, After.class).orElse(null))
        .withScenarios(null)
        .withDsls(dsls)
        .withDSLMethods(dslMethods)
        .withTestInstance(testInstance)
        .withGrafana(grafanaInfo)
        .build();
  }

  private DslMethod createDslMethod(Object testInstance, Method method) {
    return new DslMethodImpl(getName(method), ReflectionUtils.executeMethod(method, testInstance));
  }

  private String getName(Method method) {
    var name = method.getDeclaredAnnotation(Dsl.class).name();
    DslBuilder.dslMethodName.set(name);
    return name;
  }

  private DslBuilder getLoadDsl(Pair<String, DslBuilder> pair) {
    var loadDsl = pair.getSecond();
    if (loadDsl instanceof DslBuilderImpl) {
      return ((DslBuilderImpl) loadDsl).withName(pair.getFirst());
    }
    return loadDsl;
  }

  private GrafanaInfo getGrafanaInfo(Class clazz) {
    var grafanaAnnotation = (Grafana) clazz.<Grafana>getDeclaredAnnotation(Grafana.class);
    if (grafanaAnnotation != null) {
      return new GrafanaInfo(grafanaAnnotation.dashboard(), grafanaAnnotation.name());
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

  private boolean isEnabled(Method method) {
    return method.getDeclaredAnnotation(Disabled.class) == null;
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
}
