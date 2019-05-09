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

import io.ryos.rhino.sdk.data.ContextImpl;
import io.ryos.rhino.sdk.utils.Environment;
import io.ryos.rhino.sdk.data.Context;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Supervisor type which manages set up and run benchmark tests. The class follows the steps
 * required to initiate a test execution i.e configure and search for benchmark jobs by using
 * {@link SimulationJobsScanner}. Once jobs are ready to execute, the implementation starts each
 * one while providing a context to them.
 *
 * <p>A {@link Context} instance is a storage associated with each
 * benchmark job.
 *
 * @author Erhan Bagdemir
 * @see SimulationConfig
 * @see SimulationJobsScanner
 * @see Context
 * @since 1.0
 */
public class SimulationSpecImpl implements SimulationSpec {

  private static Logger LOG = LogManager.getLogger(Simulation.class);
  private static final String JOB = "job";

  /**
   * A list of simulation runner instances.
   */
  private final List<SimulationRunner> simulationRunners;

  /**
   * Constructs a new instance of {@link SimulationSpecImpl}.
   *
   * @param path Path to properties file.
   * @param simulationName Simulation name.
   */
  public SimulationSpecImpl(final String path, final String simulationName) {

    Application.showBranding();

    final Environment environment = getEnvironment();

    var simulationConfig = SimulationConfig.newInstance(path, environment);
    var jobs = SimulationJobsScanner.create().scan(simulationName,
        simulationConfig.getPackageToScan());
    this.simulationRunners = jobs
        .stream()
        .map(this::getContext)
        .map(SimulationRunnerImpl::new)
        .collect(Collectors.toList());
  }

  private Environment getEnvironment() {

    final String profile = System.getProperty("profile", "DEV");

    try {
      return Environment.valueOf(profile);
    } catch (IllegalArgumentException e) {
      System.out.println("! Profile not found: " + profile);
    }

    System.exit(-1);

    return null;
  }

  private ContextImpl getContext(final Simulation job) {
    var context = new ContextImpl();
    context.add(JOB, job);
    return context;
  }

  @Override
  public void start() {
    if (simulationRunners.isEmpty()) {
      System.out
          .println("No Simulation entity was found in package: " + SimulationConfig.getPackage());
    }
    simulationRunners.forEach(SimulationRunner::start);
  }

  @Override
  public void stop() {
    System.out.println("Stopping simulation...");
    simulationRunners.forEach(SimulationRunner::stop);
  }
}
