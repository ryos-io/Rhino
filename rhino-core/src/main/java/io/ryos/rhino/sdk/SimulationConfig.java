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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Simulation configuration instances are used to configure benchmark tests. The {@link
 * SimulationConfig} instances are passed the configuration parameters to construct the {@link
 * Simulation} objects. Once the {@link Simulation} is fully configured, the
 * instances thereof are ready to run which starts off the benchmark test.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see Simulation
 * @since 1.0.0
 */
public class SimulationConfig {

  private static final String PACKAGE_TO_SCAN = "packageToScan";
  private static final int PAR_RATIO = 5;
  private static final int MAX_PAR = 1000;
  private static final int MAX_CONN = 1000;
  private static final String SIM_ID = "SIM_ID";
  private static final int DEFAULT_BATCH_DURATION = 200;
  private static final int DEFAULT_BATCH_ACTIONS = 1000;
  private static final String DEFAULT_TIMEOUT = "60000";
  private static final String DEFAULT_CONNECTIONS = "1000";
  private static final String DEFAULT_READ_TIMEOUT = "15000";
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

    try (var is = new ConfigResource(path).getInputStream()) {
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

  public static int getInfluxBatchActions() {
    if (instance.getDBSupportInfluxBatchActions() != null) {
      return Integer.parseInt(instance.getDBSupportInfluxBatchActions());
    }
    return DEFAULT_BATCH_ACTIONS;
  }

  public static int getInfluxBatchDuration() {
    if (instance.getDBSupportInfluxBatchDuration() != null) {
      return Integer.parseInt(instance.getDBSupportInfluxBatchDuration());
    }
    return DEFAULT_BATCH_DURATION;
  }

  public static String getInfluxRetentionPolicy() {
    return instance.getDBSupportInfluxRetentionPolicy();
  }

  private String getNodeName() {
    return properties.getProperty("node");
  }

  private UserSource.SourceType getUsersSource() {
    var source = properties.getProperty(environment + ".auth.users.source", "file");
    return UserSource.SourceType.valueOf(source.toUpperCase());
  }

  private String getAuthUserFileSource() {
    return properties.getProperty(environment + ".auth.users.file");
  }

  private String getConfigHttpMaxConnections() {
    return properties.getProperty("http.maxConnections",
        DEFAULT_CONNECTIONS);
  }

  private String getConfigHttpConnectTimeout() {
    return properties.getProperty("http.connectTimeout",
        DEFAULT_TIMEOUT);
  }

  private String getConfigHttpReadTimeout() {
    return properties.getProperty("http.readTimeout",
        DEFAULT_READ_TIMEOUT);
  }

  private String getConfigHttpHandshakeTimeout() {
    return properties.getProperty("http.handshakeTimeout",
        DEFAULT_TIMEOUT);
  }

  private String getConfigHttpRequestTimeout() {
    return properties.getProperty("http.requestTimeout",
        DEFAULT_TIMEOUT);
  }

  private String getRunnerParallelisation() {
    return properties.getProperty("runner.parallelisim",
        Integer.toString(Runtime.getRuntime().availableProcessors() * PAR_RATIO));
  }

  private String getAuthClientId() {
    return properties.getProperty(environment + ".oauth2.clientId");
  }

  private String getAuthApiKey() {
    return properties.getProperty(environment + ".oauth2.apiKey");
  }

  private String getAuthEndpoint() {
    return properties.getProperty(environment + ".oauth2.endpoint");
  }

  private String getAuthClientSecret() {
    return properties.getProperty(environment + ".oauth2.clientSecret");
  }

  private String getAuthClientCode() {
    return properties.getProperty(environment + ".oauth2.clientCode");
  }

  private String getServiceAuthEnabled() {
    return properties.getProperty(environment + ".oauth2.service.authentication");
  }

  private String getServiceAuthClientId() {
    return properties.getProperty(environment + ".oauth2.service.clientId");
  }

  private String getServiceAuthClientCode() {
    return properties.getProperty(environment + ".oauth2.service.clientCode");
  }

  private String getAuthBearerType() {
    return properties.getProperty(environment + ".oauth2.bearer");
  }

  private String getAuthHeaderName() {
    return properties.getProperty(environment + ".oauth2.headerName");
  }

  private String getServiceAuthGrantType() {
    return properties.getProperty(environment + ".oauth2.service.grantType");
  }

  private String getServiceAuthClientSecret() {
    return properties.getProperty(environment + ".oauth2.service.clientSecret");
  }

  private String getAuthGrantType() {
    return properties.getProperty(environment + ".oauth2.grantType");
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

  private String getEndpoint() {
    return properties.getProperty(environment + ".endpoint");
  }

  private String _getInMemoryFileProviderMaxSize() {
    return properties.getProperty("provider.RandomInMemoryFile.maxSize");
  }

  private String _getInMemoryFileProviderNumberOfFiles() {
    return properties.getProperty("provider.RandomInMemoryFile.numberOfFiles");
  }

  private String _getInMemoryFileProviderMimeTypes() {
    return properties.getProperty("provider.RandomInMemoryFile.mimeTypes");
  }

  private String getDBSupportInfluxBatchActions() {
    return properties.getProperty("db.influx.batch.actions");
  }

  private String getDBSupportInfluxBatchDuration() {
    return properties.getProperty("db.influx.batch.duration");
  }

  private String getDBSupportInfluxRetentionPolicy() {
    return properties.getProperty("db.influx.policy");
  }

  private String getSimId() {
    return simulationId;
  }

  String getPackageToScan() {
    return instance.getProperty(PACKAGE_TO_SCAN);
  }

  public static int getInMemoryFileProviderMaxSize() {
    return Integer.valueOf(instance._getInMemoryFileProviderMaxSize());
  }

  public static int getInMemoryFileProviderNumberOfFiles() {
    return Integer.valueOf(instance._getInMemoryFileProviderNumberOfFiles());
  }

  public static List<String> getInMemoryFileProviderMimeTypes() {
    String mimeTypes = instance._getInMemoryFileProviderMimeTypes();
    if (mimeTypes != null) {
      return Arrays.stream(mimeTypes.split(",")).map(String::trim).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public static int getMaxConnections() {
    var maxConnection = instance.getConfigHttpMaxConnections();
    return Integer.min(Integer.parseInt(maxConnection), MAX_CONN);
  }

  public static int getHttpConnectTimeout() {
    return Integer.parseInt(instance.getConfigHttpConnectTimeout());
  }

  public static int getHttpReadTimeout() {
    return Integer.parseInt(instance.getConfigHttpReadTimeout());
  }

  public static int getHttpHandshakeTimeout() {
    return Integer.parseInt(instance.getConfigHttpHandshakeTimeout());
  }

  public static int getHttpRequestTimeout() {
    return Integer.parseInt(instance.getConfigHttpRequestTimeout());
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

  public static String getClientCode() {
    return instance.getAuthClientCode();
  }

  public static String getGrantType() {
    return instance.getAuthGrantType();
  }

  public static String getApiKey() {
    return instance.getAuthApiKey();
  }

  public static String getAuthServer() {
    String authServerOverride = System.getProperty("test.oauth2.endpoint");
    if (authServerOverride != null) {
      return authServerOverride;
    }
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

  public static String getServiceClientId() {
    return instance.getServiceAuthClientId();
  }

  public static String getServiceClientCode() {
    return instance.getServiceAuthClientCode();
  }

  public static String getServiceGrantType() {
    return instance.getServiceAuthGrantType();
  }

  public static String getServiceClientSecret() {
    return instance.getServiceAuthClientSecret();
  }

  public static boolean isServiceAuthenticationEnabled() {
    return "true".equalsIgnoreCase(instance.getServiceAuthEnabled());
  }

  public static String getBearerType() {
    return instance.getAuthBearerType();
  }

  public static String getHeaderName() {
    return instance.getAuthHeaderName();
  }
}
