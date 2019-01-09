/* ************************************************************************
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

import com.adobe.rhino.sdk.data.ContextImpl;
import com.adobe.rhino.sdk.utils.Environment;
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
 * <p>A {@link com.adobe.rhino.sdk.data.Context} instance is a storage associated with each
 * benchmark job.
 *
 * @author Erhan Bagdemir
 * @see SimulationConfig
 * @see SimulationJobsScanner
 * @see com.adobe.rhino.sdk.data.Context
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
