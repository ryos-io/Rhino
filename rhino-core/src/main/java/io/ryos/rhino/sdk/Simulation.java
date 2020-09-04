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

import io.ryos.rhino.sdk.data.Context;
import io.ryos.rhino.sdk.data.ContextImpl;
import io.ryos.rhino.sdk.data.Pair;
import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import io.ryos.rhino.sdk.exceptions.ProfileNotFoundException;
import io.ryos.rhino.sdk.exceptions.SimulationNotFoundException;
import io.ryos.rhino.sdk.runners.SimulationRunner;
import io.ryos.rhino.sdk.utils.Environment;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supervisor which manages set up and run benchmark tests. The class follows the steps required to
 * initiate a test execution i.e configure and search for benchmark jobs by using {@link
 * SimulationJobsScanner}. Once jobs are ready to execute, the implementation starts each one while
 * providing a context to them.
 *
 * <p>A {@link Context} instance is a storage associated with each
 * benchmark job.
 *
 * @author Erhan Bagdemir
 * @see SimulationConfig
 * @see SimulationJobsScanner
 * @see Context
 * @since 1.0.0
 */
public class Simulation {

  private static final Logger LOG = LoggerFactory.getLogger(Simulation.class);
  private static final String KEY_PROFILE = "profile";
  private static final String JOB = "job";
  private static Simulation INSTANCE;

  private List<SimulationRunner> simulationRunners;
  private SimulationMetadata metadata;

  private Simulation(final String path, final Class<?> simulationClass) {
    try {
      Runtime.getRuntime().addShutdownHook(new Thread(Simulation.this::stop));
      Application.showBranding();
      initializeConfiguration(path);

      this.metadata = extractJobFromClass(simulationClass);
      this.simulationRunners = getSimulationRunners(Collections.singletonList(metadata));
    } catch (Exception pe) {
      LOG.error("Cannot start application", pe);
      System.exit(-1);
    }
  }

  public static Simulation getInstance(final String propsPath, final Class<?> simulationClass) {
    if (INSTANCE == null) {
      INSTANCE = new Simulation(propsPath, simulationClass);
    }
    return INSTANCE;
  }

  public static Optional<SimulationMetadata> getData() {
    if (INSTANCE != null) {
      return Optional.of(INSTANCE.getMetadata());
    }
    return Optional.empty();
  }

  private void initializeConfiguration(final String path) {
    SimulationConfig.newInstance(path, getEnvironment());
  }

  private SimulationMetadata extractJobFromClass(final Class<?> simulationClass) {
    return SimulationJobsScanner.create().createBenchmarkJob(simulationClass);
  }

  private List<SimulationRunner> getSimulationRunners(final List<SimulationMetadata> jobs) {
    return jobs.stream()
        .map(
            simulation -> new Pair<SimulationMetadata, Context>(simulation, getContext(simulation)))
        .map(pair -> getRunner(pair.getFirst().getRunner(), pair.getSecond()))
        .collect(Collectors.toList());
  }

  private SimulationRunner getRunner(final Class<? extends SimulationRunner> runnerClass,
      final Context context) {
    try {
      var declaredConstructor = runnerClass.getDeclaredConstructor(Context.class);
      return declaredConstructor.newInstance(context);
    } catch (NoSuchMethodException nsm) {
      // implement default constructor creation.
    } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
      LOG.error("Cannot get runner implementation", e);
    }

    throw new IllegalArgumentException("Runner class: " + runnerClass.getName() + " is invalid.");
  }

  private Environment getEnvironment() {

    var profile = System.getProperty(KEY_PROFILE, Environment.DEV.toString());

    try {
      return Environment.valueOf(profile.toUpperCase());
    } catch (IllegalArgumentException e) {
      ExceptionUtils.rethrow(e, ProfileNotFoundException.class, "ERROR: Environment profile '"
          + profile + "' not found. Dev, Stage, Prod are known environment profiles. Pass "
          + "a valid VM argument e.g -Dprofile=dev");
    }
    return null;
  }

  private ContextImpl getContext(final SimulationMetadata job) {
    var context = new ContextImpl();
    context.add(JOB, job);
    return context;
  }

  public void verify() {
    try {
      verifyRunners();
      simulationRunners.forEach(SimulationRunner::verify);
    } catch (Exception t) {
      LOG.error("Cannot start application", t);
      System.exit(-1);
    }
  }

  public void times(int numberOfRepeats) {
    try {
      verifyRunners();
      simulationRunners.forEach(t -> t.times(numberOfRepeats));
    } catch (Exception t) {
      LOG.error("Cannot start application", t);
      System.exit(-1);
    }
  }

  public void start() {
    try {
      verifyRunners();
      simulationRunners.forEach(SimulationRunner::start);
    } catch (Exception t) {
      LOG.error("Cannot start application", t);
      System.exit(-1);
    }
  }

  private void verifyRunners() {
    if (simulationRunners.isEmpty()) {
      throw new SimulationNotFoundException(
          "ERROR: No simulation found in '" + SimulationConfig.getPackage() + "'.");
    }
  }

  public void stop() {
    LOG.info("Stopping simulation...");
    if (simulationRunners != null) {
      simulationRunners.forEach(SimulationRunner::stop);
    }
  }

  public SimulationMetadata getMetadata() {
    return metadata;
  }
}
