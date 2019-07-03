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

import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import io.ryos.rhino.sdk.exceptions.RhinoIOException;
import io.ryos.rhino.sdk.io.ConfigResource;
import io.ryos.rhino.sdk.users.source.UserSource;
import io.ryos.rhino.sdk.users.source.UserSource.SourceType;
import io.ryos.rhino.sdk.utils.Environment;
import io.ryos.rhino.sdk.validators.PropsValidatorImpl;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

/**
 * Simulation configuration instances are used to configure benchmark tests. The {@link
 * SimulationConfig} instances are passed the configuration parameters to construct the {@link
 * SimulationImpl} objects. Once the {@link SimulationImpl} is fully configured, the
 * instances thereof are ready to run which starts off the benchmark test.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see SimulationImpl
 * @since 1.0
 */
public class SimulationConfig {

  private static final String PACKAGE_TO_SCAN = "packageToScan";
  private static final int PAR_RATIO = 5;
  private static final int MAX_PAR = 1000;
  private static final int MAX_CONN = 1000;
  private static final String SIM_ID = "SIM_ID";
  private static SimulationConfig instance;

  private final Properties properties;
  private final String environment;
  private final String simulationId;

  private SimulationConfig(final String path, final Environment environment) {
    this.properties = new Properties();
    this.environment = environment.toString();
    this.simulationId = Optional
            .ofNullable(System.getenv().get(SIM_ID))
            .orElse(UUID.randomUUID().toString());

    loadConfig(path);
  }

  static SimulationConfig newInstance(final String path, final Environment environment) {
    if (instance != null) {
      return instance;
    }

    instance = new SimulationConfig(path, environment);

    return instance;
  }

  private void loadConfig(final String path) {

    try(var is = new ConfigResource(path).getInputStream()) {
      properties.load(is);

      var propsValidator = new PropsValidatorImpl();
      propsValidator.validate(properties);

    } catch (IOException e) {
      ExceptionUtils.rethrow(e, RhinoIOException.class, "Cannot read rhino.properties file.");
    }
  }

  private String getProperty(final String prop) {
    return properties.getProperty(prop);
  }

  private String getGrafanaURL() {
    return properties.getProperty("grafana.endpoint");
  }

  private String grafanaToken() {
    return properties.getProperty("grafana.token");
  }

  private String grafanaUsername() {
    return properties.getProperty("grafana.username");
  }

  private String grafanaPassword() {
    return properties.getProperty("grafana.password");
  }

  // Influx DB Configuration
  private String getDBSupportInfluxURL() {
    return properties.getProperty("db.influx.url");
  }

  private String getDBSupportInfluxDBName() {
    return properties.getProperty("db.influx.dbName");
  }

  private String getDBSupportInfluxUsername() {
    return properties.getProperty("db.influx.username");
  }

  private String getDBSupportInfluxPassword() {
    return properties.getProperty("db.influx.password");
  }

  private String getNodeName() {
    return properties.getProperty("node");
  }

  private UserSource.SourceType getUsersSource() {
    var source = properties.getProperty(environment + ".users.source", "file");
    return UserSource.SourceType.valueOf(source.toUpperCase());
  }

  private String getMaxConnection() {
    return properties.getProperty("reactive.maxConnections",
        "1000");
  }

  private String getRunnerParallelisation() {
    return properties.getProperty("runner.parallelisim",
        Integer.toString(Runtime.getRuntime().availableProcessors() * PAR_RATIO));
  }

  private String getAuthUserFileSource() {
    return properties.getProperty(environment + ".users.file");
  }

  private String getAuthClientId() {
    return properties.getProperty(environment + ".oauth.clientId");
  }

  private String getAuthApiKey() {
    return properties.getProperty(environment + ".oauth.apiKey");
  }

  private String getAuthEndpoint() {
    return properties.getProperty(environment + ".oauth.endpoint");
  }

  private String getAuthVaultEndpoint() {
    return properties.getProperty(environment + ".users.vault.endpoint");
  }

  private String getAuthVaultToken() {
    return properties.getProperty(environment + ".users.vault.token");
  }

  private String getAuthVaultPath() {
    return properties.getProperty(environment + ".users.vault.path");
  }

  private String getAuthVaultKey() {
    return properties.getProperty(environment + ".users.vault.key");
  }

  private String getAuthClientSecret() {
    return properties.getProperty(environment + ".oauth.clientSecret");
  }

  private String getEndpoint() {
    return properties.getProperty(environment + ".endpoint");
  }

  private String getAuthGrantType() {
    return properties.getProperty(environment + ".oauth.grantType");
  }

  private String getSimId() {
    return simulationId;
  }

  String getPackageToScan() {
    return instance.getProperty(PACKAGE_TO_SCAN);
  }

  public static int getMaxConnections() {
    var maxConnection = instance.getMaxConnection();
    return Integer.min(Integer.parseInt(maxConnection), MAX_CONN);
  }


  public static int getParallelisation() {
    var runnerParallelisation = instance.getRunnerParallelisation();
    var par = Integer.parseInt(runnerParallelisation);
    return Integer.min(par, MAX_PAR);
  }

  public static SourceType getUserSource() {
    return instance.getUsersSource();
  }

  public static String getServiceEndpoint() {
    return instance.getEndpoint();
  }

  public static String getPackage() {
    return instance.getPackageToScan();
  }

  public static String getClientId() {
    return instance.getAuthClientId();
  }

  public static String getClientSecret() {
    return instance.getAuthClientSecret();
  }

  public static String getGrantType() {
    return instance.getAuthGrantType();
  }

  public static String getApiKey() {
    return instance.getAuthApiKey();
  }

  public static String getAuthServer() {
    return instance.getAuthEndpoint();
  }

  public static String getUserFileSource() {
    return instance.getAuthUserFileSource();
  }

  public static String getInfluxURL() {
    return instance.getDBSupportInfluxURL();
  }

  public static String getInfluxDBName() {
    return instance.getDBSupportInfluxDBName();
  }

  public static String getInfluxUsername() {
    return instance.getDBSupportInfluxUsername();
  }

  public static String getInfluxPassword() {
    return instance.getDBSupportInfluxPassword();
  }

  public static String getVaultEndpoint() {
    return instance.getAuthVaultEndpoint();
  }

  public static String getVaultToken() {
    return instance.getAuthVaultToken();
  }

  public static String getVaultPath() {
    return instance.getAuthVaultPath();
  }

  public static String getNode() {
    return instance.getNodeName();
  }

  public static String getSimulationId() {
    return instance.getSimId();
  }

  public static String getGrafanaEndpoint() {
    return instance.getGrafanaURL();
  }

  public static String getGrafanaToken() {
    return instance.grafanaToken();
  }

  public static String getGrafanaUser() {
    return instance.grafanaUsername();
  }

  public static String getGrafanaPassword() {
    return instance.grafanaPassword();
  }
}
