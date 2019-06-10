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

import static io.ryos.rhino.sdk.utils.ReflectionUtils.getFieldByAnnotation;
import static io.ryos.rhino.sdk.utils.ReflectionUtils.instanceOf;
import static java.util.stream.Collectors.toList;

import io.ryos.rhino.sdk.annotations.After;
import io.ryos.rhino.sdk.annotations.Before;
import io.ryos.rhino.sdk.annotations.CleanUp;
import io.ryos.rhino.sdk.annotations.Influx;
import io.ryos.rhino.sdk.annotations.Logging;
import io.ryos.rhino.sdk.annotations.Prepare;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.TestSpec;
import io.ryos.rhino.sdk.annotations.UserFeeder;
import io.ryos.rhino.sdk.data.Pair;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.exceptions.RepositoryNotFoundException;
import io.ryos.rhino.sdk.exceptions.SimulationNotFoundException;
import io.ryos.rhino.sdk.runners.DefaultSimulationRunner;
import io.ryos.rhino.sdk.exceptions.SpecificationNotFoundException;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.specs.Spec;
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
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimulationJobsScannerImpl implements SimulationJobsScanner {

  private static final Logger LOG = LogManager.getLogger(SimulationJobsScannerImpl.class);
  private static final String DOT = ".";

  @Override
  public List<SimulationMetadata> scan(String forSimulation, String... inPackages) {
    Objects.requireNonNull(inPackages, "inPackages must not be null.");
    Objects.requireNonNull(forSimulation, "forSimulation must not be null.");

    return Arrays.stream(inPackages)
        .map(p -> p.replace(DOT, File.separator))
        .flatMap(p -> scanBenchmarkClassesIn(p).stream()
            .filter(a -> forSimulation.equals(getSimulationName(a))))
        .map(this::createBenchmarkJob)
        .collect(toList());
  }

  private List<Class> scanBenchmarkClassesIn(String path) {

    try {
      final URL resource = getClass().getClassLoader().getResource(path);
      if (resource == null) {
        return Collections.emptyList();
      }

      if (isJarFile(resource)) {
        return getBenchmarkClassesFromJar(resource, path);
      }

      // Search for classes in development environment. The IDE runs the project in an exploded
      // directory, so no need to scan the JAR file.
      var resourceURL = Optional.ofNullable(getClass().getClassLoader().getResource(path)).orElseThrow();
      var files = new File(resourceURL.toURI()).listFiles();
      if (files != null) {
        return Arrays.stream(files).
            filter(File::isFile).
            map(File::getName).
            map(f -> buildClassNameFrom(path, f)).
            map(this::getClassFor).filter(this::isBenchmarkClass).collect(toList());
      }
    } catch (URISyntaxException e) {
      LOG.error("URL syntax not valid.", e);
    }
    return Collections.emptyList();
  }

  // Search for benchmark classes within the JAR. The benchmark project will be packaged as JAR, so
  // the JarURLConnection is used to traverse within the artifact.
  private List<Class> getBenchmarkClassesFromJar(final URL resource, final String inPath) {
    var result = new ArrayList<Class>();

    JarURLConnection urlConnection;

    try {
      urlConnection = (JarURLConnection) new URL(resource.toExternalForm()).openConnection();
      var entries = urlConnection.getJarFile().entries();

      while (entries.hasMoreElements()) {
        var jarEntry = entries.nextElement();
        var jarEntryName = jarEntry.getRealName();
        // only use class entries.
        if (jarEntryName.contains(inPath) && jarEntryName.endsWith(".class")) {
          var className = jarEntryName.substring(0, jarEntryName.lastIndexOf(DOT))
              .replace(File.separator, DOT);
          final Class classFor = getClassFor(className);
          if (isBenchmarkClass(classFor)) {
            result.add(classFor);
          }
        }
      }
    } catch (IOException e) {
      LOG.error("Cannot scan the JAR file.", e);
    }
    return result;
  }

  private boolean isJarFile(final URL resource) {
    final String resourceURL = resource.toExternalForm();
    return resourceURL != null && (resourceURL.contains(".jar!"));
  }

  // transform the path into package format, to be able to build the fully qualified class name.
  private String buildClassNameFrom(String path, String className) {
    return path.replace(File.separator, DOT) + DOT + className.substring(0, className.indexOf(DOT));
  }

  private boolean isBenchmarkClass(Class clazz) {
    return Arrays.stream(clazz.getDeclaredAnnotations())
        .anyMatch(f -> f instanceof io.ryos.rhino.sdk.annotations.Simulation);
  }

  private String getSimulationName(Class clazz) {
    return Arrays.stream(clazz.getDeclaredAnnotations())
        .filter(f -> f instanceof io.ryos.rhino.sdk.annotations.Simulation)
        .findFirst()
        .map(s -> ((io.ryos.rhino.sdk.annotations.Simulation) s).name())
        .orElse(null);
  }

  private Class getClassFor(String name) {
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private SimulationMetadata createBenchmarkJob(final Class clazz) {

    var simAnnotation = (io.ryos.rhino.sdk.annotations.Simulation) clazz
        .getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.Simulation.class);
    var runnerAnnotation = (io.ryos.rhino.sdk.annotations.Runner) clazz
        .getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.Runner.class);
    var enableInflux = clazz.getDeclaredAnnotation(Influx.class) != null;
    var stepMethods = Arrays.stream(clazz.getDeclaredMethods())
        .filter(m -> Arrays.stream(m.getDeclaredAnnotations())
            .anyMatch(a -> a instanceof io.ryos.rhino.sdk.annotations.Scenario))
        .map(s -> new Scenario(
            s.getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.Scenario.class).name(), s))
        .collect(toList());
    var specMethods = Arrays.stream(clazz.getDeclaredMethods())
        .filter(m -> Arrays.stream(m.getDeclaredAnnotations())
            .anyMatch(a -> a instanceof io.ryos.rhino.sdk.annotations.TestSpec))
        .map(s -> new Pair<String, Spec>(s.getDeclaredAnnotation(TestSpec.class).name(),
            ReflectionUtils.<Spec>executeMethod(s,
            instanceOf(clazz).orElseThrow())))
        .map(p -> p.getSecond().withSpecName(p.getFirst()))
        .collect(toList());

    if (stepMethods.isEmpty() && isBlockingSimulation(runnerAnnotation)) {
      throw new SimulationNotFoundException(clazz.getName());
    }

    if (specMethods.isEmpty() && isReactiveSimulation(runnerAnnotation)) {
      throw new SpecificationNotFoundException(clazz.getName());
    }

    var loggingAnnotation = (Logging) clazz.getDeclaredAnnotation(Logging.class);
    var logger = Optional.ofNullable(loggingAnnotation).map(Logging::file).orElse(null);
    var injectAnnotationField = getFieldByAnnotation(clazz, UserFeeder.class);
    var maxUserInject = injectAnnotationField.map(p -> p.getSecond().max()).orElse(10);
    var userRepo = injectAnnotationField.map(p -> createUserRepository(p.getSecond()))
        .orElse(new DefaultUserRepositoryFactoryImpl().create());

    return new SimulationMetadata.Builder()
        .withSimulationClass(clazz)
        .withUserRepository(userRepo)
        .withRunner(runnerAnnotation != null ? runnerAnnotation.clazz() :
            DefaultSimulationRunner.class)
        .withSimulation(simAnnotation.name())
        .withDuration(Duration.ofMinutes(simAnnotation.durationInMins()))
        .withInjectUser(maxUserInject)
        .withLogWriter(validateLogFile(logger))
        .withInflux(enableInflux)
        .withPrepare(findMethodWith(clazz, Prepare.class).orElse(null))
        .withCleanUp(findMethodWith(clazz, CleanUp.class).orElse(null))
        .withBefore(findMethodWith(clazz, Before.class).orElse(null))
        .withAfter(findMethodWith(clazz, After.class).orElse(null))
        .withScenarios(stepMethods)
        .withSpecs(specMethods)
        .withRampUp(-1). // Throttling is not scope of 1.0 anymore.
        build();
  }

  private boolean isBlockingSimulation(final Runner runnerAnnotation) {
    return runnerAnnotation.clazz().equals(DefaultSimulationRunner.class);
  }

  private boolean isReactiveSimulation(final Runner runnerAnnotation) {
    return runnerAnnotation.clazz().equals(ReactiveHttpSimulationRunner.class);
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
      System.err.println(String.format("! Simulation log file is invalid: \"%s\"", simFile));
      System.exit(-1);
    }
    return logFile;
  }

  private UserRepository createUserRepository(final UserFeeder feeder) {

    var factory = feeder.factory();
    var loginDelay = feeder.delay();

    try {
      final Constructor<? extends UserRepositoryFactory> factoryConstructor =
          factory.getConstructor(long.class);
      final UserRepositoryFactory userRepositoryFactory = factoryConstructor
          .newInstance(loginDelay);
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
      throw new RuntimeException(e);
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