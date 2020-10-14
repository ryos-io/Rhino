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

import static io.ryos.rhino.sdk.utils.ReflectionUtils.getClassLevelAnnotation;
import static io.ryos.rhino.sdk.utils.ReflectionUtils.instanceOf;

import io.ryos.rhino.sdk.annotations.Logging;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.DslBuilder;
import io.ryos.rhino.sdk.dsl.DslMethod;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.reporting.SimulationLogFormatter;
import io.ryos.rhino.sdk.runners.SimulationRunner;
import io.ryos.rhino.sdk.users.repositories.UserRepository;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * {@link SimulationMetadata} is representation of a single performance testing job. The instances
 * of {@link SimulationMetadata} is created by using the metadata provided on annotated benchmark
 * entities. Simulation metadata entities do comprise scenarios, that are run per user on a single
 * thread. For each scenario there will be a new SimulationMetadata instance created so as to run
 * the scenario isolated on a single thread.
 * <p>
 *
 * The job instances are created by {@link SimulationJobsScanner} classes.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see io.ryos.rhino.sdk.annotations.Simulation
 * @since 1.0.0
 */
public class SimulationMetadata {

  /**
   * Runner class type.
   * <p>
   */
  private Class<? extends SimulationRunner> runner;

  /**
   * The name of the simulation, used in reports as well as to start a specific simulation among
   * others, selectively.
   * <p>
   */
  private String simulationName;

  /**
   * Duration of the simulation in minutes.
   * <p>
   */
  private Duration duration;

  /**
   * The number of users to be injected during the benchmark job execution. It is the maximum number
   * of users making benchmark requests against the back-end.
   * <p>
   */
  private int numberOfUsers;

  /**
   * User region is the region of the users that simulates the load.
   * <p>
   */
  private String userRegion;

  /**
   * Throttling info.
   * <p>
   */
  private ThrottlingInfo throttlingInfo;

  /**
   * Grafana configuration.
   * <p>
   */
  private GrafanaInfo grafanaInfo;

  /**
   * Simulation class.
   * <p>
   */
  private Class simulationClass;

  /**
   * Test instance.
   * <p>
   */
  private Object testInstance;

  /**
   * The {@link java.lang.reflect.Method} instance for running the test.
   * <p>
   */
  private List<Scenario> scenarios;

  /**
   * A list of {@link DslBuilder} instances defined in getLoadDsls methods.
   * <p>
   */
  private List<DslBuilder> dslBuilders;

  private List<DslMethod> dslMethods;

  /**
   * The {@link java.lang.reflect.Method} instance for preparing the scenario.
   * <p>
   */
  private Method beforeMethod;

  /**
   * The {@link java.lang.reflect.Method} instance for cleaning up the scenario. The clean up method
   * will be run after scenario test execution.
   * <p>
   */
  private Method afterMethod;

  /**
   * The {@link java.lang.reflect.Method} instance for preparing the simulation.
   * <p>
   */
  private Method prepareMethod;

  /**
   * The {@link java.lang.reflect.Method} instance for cleaning up the test. The clean up method
   * will be run after performance simulation execution.
   * <p>
   */
  private Method cleanupMethod;

  /**
   * User repository.
   * <p>
   */
  private UserRepository<UserSession> userRepository;

  /**
   * Enable Influx DB integration.
   * <p>
   */
  private boolean enableInflux;

  private String reportingURI;

  /**
   * Uses a builder to construct the instance.
   * <p>
   */
  private SimulationMetadata(final Builder builder) {
    this.duration = builder.duration;
    this.simulationName = builder.simulation;
    this.testInstance = builder.testInstance;
    this.numberOfUsers = builder.injectUser;
    this.simulationClass = builder.simulationClass;
    this.scenarios = builder.scenarios;
    this.dslBuilders = builder.dslBuilders;
    this.dslMethods = builder.dslMethods;
    this.prepareMethod = builder.prepareMethod;
    this.cleanupMethod = builder.cleanUpMethod;
    this.beforeMethod = builder.beforeMethod;
    this.afterMethod = builder.afterMethod;
    this.userRepository = builder.userRepository;
    this.enableInflux = builder.enableInflux;
    this.runner = builder.runner;
    this.reportingURI = builder.reportingURI;
    this.throttlingInfo = builder.throttlingInfo;
    this.userRegion = builder.userRegion;
    this.grafanaInfo = builder.grafanaInfo;
  }

  public SimulationLogFormatter getLogFormatter() {
    var loggingAnnotation = getClassLevelAnnotation(simulationClass, Logging.class);
    var logging = loggingAnnotation.orElseGet(() -> null);

    if (logging == null) {
      return null;
    }

    final Optional<? extends SimulationLogFormatter> logFormatterInstance = instanceOf(
        logging.formatter());
    return logFormatterInstance.orElseThrow(RuntimeException::new);
  }

  public Method getPrepareMethod() {
    return prepareMethod;
  }

  public Method getCleanupMethod() {
    return cleanupMethod;
  }

  public String getReportingURI() {
    return reportingURI;
  }

  public Class<? extends SimulationRunner> getRunner() {
    return runner;
  }

  public int getNumberOfUsers() {
    return numberOfUsers;
  }

  public UserRepository<UserSession> getUserRepository() {
    return userRepository;
  }

  public Duration getDuration() {
    return duration;
  }

  public List<Scenario> getScenarios() {
    return scenarios;
  }

  public List<DslBuilder> getDsls() {
    return dslBuilders;
  }

  public List<DslMethod> getDslMethods() {
    return dslMethods;
  }

  public Class getSimulationClass() {
    return simulationClass;
  }

  public boolean isEnableInflux() {
    return enableInflux;
  }

  public Method getBeforeMethod() {
    return beforeMethod;
  }

  public Method getAfterMethod() {
    return afterMethod;
  }

  public String getSimulationName() {
    return simulationName;
  }

  public Object getTestInstance() {
    return testInstance;
  }

  public ThrottlingInfo getThrottlingInfo() {
    return throttlingInfo;
  }

  public String getUserRegion() {
    return userRegion;
  }

  public GrafanaInfo getGrafanaInfo() {
    return grafanaInfo;
  }

  /**
   * Builder for {@link SimulationMetadata}.
   * <p>
   */
  static class Builder {

    /**
     * Simulation name.
     * <p>
     */
    private String simulation;

    /**
     * The total number of users to be injected.
     * <p>
     */
    private int injectUser;

    private ThrottlingInfo throttlingInfo;

    /**
     * Simulation class, is the one with the {@link io.ryos.rhino.sdk.annotations.Simulation}
     * annotation.
     * <p>
     */
    private Class<?> simulationClass;

    /**
     * Test instance.
     * <p>
     */
    private Object testInstance;

    /**
     * Runner implementation.
     * <p>
     */
    private Class<? extends SimulationRunner> runner;

    /**
     * The {@link java.lang.reflect.Method} instance of the run method.
     * <p>
     */
    private List<Scenario> scenarios;

    private List<DslMethod> dslMethods;

    /**
     * List of {@link MaterializableDslItem} instances.
     * <p>
     */
    private List<DslBuilder> dslBuilders;

    /**
     * The {@link java.lang.reflect.Method} instance for preparing the test.
     * <p>
     */
    private Method prepareMethod;

    /**
     * The {@link java.lang.reflect.Method} instance for cleaning up the test. The clean up method
     * will be run after performance test execution.
     * <p>
     */
    private Method cleanUpMethod;

    /**
     * The {@link java.lang.reflect.Method} instance for preparing the test.
     * <p>
     */
    private Method beforeMethod;

    /**
     * The {@link java.lang.reflect.Method} instance for cleaning up the test. The clean up method
     * will be run after performance test execution.
     * <p>
     */
    private Method afterMethod;

    /**
     * User repository.
     * <p>
     */
    private UserRepository<UserSession> userRepository;

    /**
     * User region.
     * <p>
     */
    private String userRegion;

    /**
     * Grafana info.
     * <p>
     */
    private GrafanaInfo grafanaInfo;

    /**
     * The reporting URI in String.
     * <p>
     */
    private String reportingURI;

    /**
     * Duration of the performance test.
     * <p>
     */
    private Duration duration;

    /**
     * Enables the influx db integration.
     * <p>
     */
    private boolean enableInflux;

    public Builder withInflux(final boolean enableInflux) {
      this.enableInflux = enableInflux;
      return this;
    }

    public Builder withGrafana(final GrafanaInfo grafanaInfo) {
      this.grafanaInfo = grafanaInfo;
      return this;
    }

    public Builder withSimulation(final String simulation) {
      this.simulation = simulation;
      return this;
    }

    public Builder withInjectUser(final int injectUser) {
      this.injectUser = injectUser;
      return this;
    }

    public Builder withThrottling(final ThrottlingInfo throttlingInfo) {
      this.throttlingInfo = throttlingInfo;
      return this;
    }

    public Builder withTestInstance(final Object testInstance) {
      this.testInstance = testInstance;
      return this;
    }

    public Builder withSimulationClass(final Class<?> simulationClass) {
      this.simulationClass = simulationClass;
      return this;
    }

    public Builder withRunner(final Class<? extends SimulationRunner> runner) {
      this.runner = runner;
      return this;
    }

    public Builder withScenarios(final List<Scenario> scenarios) {
      this.scenarios = scenarios;
      return this;
    }

    public Builder withDsls(final List<DslBuilder> dslBuilders) {
      this.dslBuilders = dslBuilders;
      return this;
    }

    public Builder withDSLMethods(final List<DslMethod> dslMethods) {
      this.dslMethods = dslMethods;
      return this;
    }

    public Builder withPrepare(final Method prepareMethod) {
      this.prepareMethod = prepareMethod;
      return this;
    }

    public Builder withCleanUp(final Method cleanUpMethod) {
      this.cleanUpMethod = cleanUpMethod;
      return this;
    }

    public Builder withBefore(final Method beforeMethod) {
      this.beforeMethod = beforeMethod;
      return this;
    }

    public Builder withAfter(final Method afterMethod) {
      this.afterMethod = afterMethod;
      return this;
    }

    public Builder withUserRepository(final UserRepository<UserSession> userRepository) {
      this.userRepository = userRepository;
      return this;
    }

    public Builder withUserRegion(final String userRegion) {
      this.userRegion = userRegion;
      return this;
    }

    public Builder withLogWriter(final String reportingURI) {
      this.reportingURI = reportingURI;
      return this;
    }

    public Builder withLogFormatter(final String logWriter) {
      this.reportingURI = logWriter;
      return this;
    }

    public Builder withDuration(final Duration duration) {
      this.duration = duration;
      return this;
    }

    public SimulationMetadata build() {
      return new SimulationMetadata(this);
    }
  }
}
