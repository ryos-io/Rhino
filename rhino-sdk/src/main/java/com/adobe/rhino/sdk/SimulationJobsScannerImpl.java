/**************************************************************************
 *                                                                        *
 * ADOBE CONFIDENTIAL                                                     *
 * ___________________                                                    *
 *                                                                        *
 *  Copyright 2018 Adobe Systems Incorporated                             *
 *  All Rights Reserved.                                                  *
 *                                                                        *
 * NOTICE:  All information contained herein is, and remains              *
 * the property of Adobe Systems Incorporated and its suppliers,          *
 * if any.  The intellectual and technical concepts contained             *
 * herein are proprietary to Adobe Systems Incorporated and its           *
 * suppliers and are protected by trade secret or copyright law.          *
 * Dissemination of this information or reproduction of this material     *
 * is strictly forbidden unless prior written permission is obtained      *
 * from Adobe Systems Incorporated.                                       *
 **************************************************************************/

package com.adobe.rhino.sdk;

import static com.adobe.rhino.sdk.utils.ReflectionUtils.getFieldByAnnotation;
import static java.util.stream.Collectors.toList;

import com.adobe.rhino.sdk.annotations.After;
import com.adobe.rhino.sdk.annotations.Before;
import com.adobe.rhino.sdk.annotations.CleanUp;
import com.adobe.rhino.sdk.annotations.Influx;
import com.adobe.rhino.sdk.annotations.Logging;
import com.adobe.rhino.sdk.annotations.Prepare;
import com.adobe.rhino.sdk.annotations.UserFeeder;
import com.adobe.rhino.sdk.data.Scenario;
import com.adobe.rhino.sdk.exceptions.SimulationNotFoundException;
import com.adobe.rhino.sdk.users.DefaultUserRepositoryFactoryImpl;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
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
  public List<Simulation> scan(String forSimulation, String... inPackages) {
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

  // SimulationSpec classes annotated with SimulationSpec annotation.
  private boolean isBenchmarkClass(Class clazz) {
    return Arrays.stream(clazz.getDeclaredAnnotations())
        .anyMatch(f -> f instanceof com.adobe.rhino.sdk.annotations.Simulation);
  }

  // SimulationSpec classes annotated with SimulationSpec annotation.
  private String getSimulationName(Class clazz) {
    return Arrays.stream(clazz.getDeclaredAnnotations())
        .filter(f -> f instanceof com.adobe.rhino.sdk.annotations.Simulation)
        .findFirst()
        .map(s -> ((com.adobe.rhino.sdk.annotations.Simulation) s).name())
        .orElse(null);
  }

  private Class getClassFor(String name) {
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private Simulation createBenchmarkJob(final Class clazz) {

    var scenarioAnnotation = (com.adobe.rhino.sdk.annotations.Simulation) clazz
        .getDeclaredAnnotation(com.adobe.rhino.sdk.annotations.Simulation.class);

    var enableInflux = clazz.getDeclaredAnnotation(Influx.class) != null;

    var stepMethods = Arrays.stream(clazz.getDeclaredMethods())
        .filter(m -> Arrays.stream(m.getDeclaredAnnotations())
        .anyMatch(a -> a instanceof com.adobe.rhino.sdk.annotations.Scenario))
        .map(s -> new Scenario(s.getDeclaredAnnotation(com.adobe.rhino.sdk.annotations.Scenario.class).name(), s))
        .collect(toList());

    if (stepMethods.isEmpty()) {
      throw new SimulationNotFoundException(clazz.getName());
    }

    var loggingAnnotation = (Logging) clazz.getDeclaredAnnotation(Logging.class);
    var logger = Optional.ofNullable(loggingAnnotation).map(Logging::file).orElse(null);
    var injectAnnotationField = getFieldByAnnotation(clazz, UserFeeder.class);
    var maxUserInject = injectAnnotationField.map(p -> p.second.max()).orElse(10);
    var userRepo = injectAnnotationField.map(p -> createUserRepository(p.second))
            .orElse(new DefaultUserRepositoryFactoryImpl().create());

    return new Simulation.Builder().
        withSimulationClass(clazz).
        withUserRepository(userRepo).
        withSimulation(scenarioAnnotation.name()).
        withDuration(scenarioAnnotation.durationInMins()).
        withInjectUser(maxUserInject).
        withLogWriter(validateLogFile(logger)).
        withInflux(enableInflux).
        withPrepare(findMethodWith(clazz, Prepare.class).orElse(null)).
        withCleanUp(findMethodWith(clazz, CleanUp.class).orElse(null)).
        withBefore(findMethodWith(clazz, Before.class).orElse(null)).
        withAfter(findMethodWith(clazz, After.class).orElse(null)).
        withScenarios(stepMethods).
        withRampUp(-1). // Throttling is not scope of 1.0 anymore.
        build();
  }

  private String validateLogFile(final String logFile) {

    var simFile = new File(logFile);

    try {
      var newFile = simFile.createNewFile();
      if (!newFile && !simFile.canWrite()) {
        throw new IOException("Not sufficient permissions to write the simulation file: " + simFile);
      }
    } catch (IOException e) {
      System.err.println(String.format("! Simulation log file is invalid: \"%s\"", simFile));
      System.exit(-1);
    }
    return logFile;
  }

  private com.adobe.rhino.sdk.users.UserRepository createUserRepository(final UserFeeder feeder) {

    var factory = feeder.factory();

    try {
      return factory.getDeclaredConstructor().newInstance().create();
    } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      LOG.error(e);
    }

    throw new RuntimeException("No user repository found.");
  }

  private <T extends Annotation> Optional<Method> findMethodWith(Class<?> clazz, Class<T> annotation) {
    return Arrays.stream(clazz.getDeclaredMethods()).
        filter(m -> Arrays.stream(m.getDeclaredAnnotations()).
            anyMatch(annotation::isInstance)).
        findFirst(); // TODO only the first step method?
  }
}