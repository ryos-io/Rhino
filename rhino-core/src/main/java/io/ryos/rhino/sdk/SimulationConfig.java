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

import io.ryos.rhino.sdk.exceptions.ConfigurationNotFoundException;
import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import io.ryos.rhino.sdk.exceptions.RhinoIOException;
import io.ryos.rhino.sdk.users.provider.UserProvider;
import io.ryos.rhino.sdk.users.provider.UserProvider.SourceType;
import io.ryos.rhino.sdk.utils.Environment;
import io.ryos.rhino.sdk.validators.PropsValidatorImpl;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simulation configuration instances are used to configure benchmark tests. The {@link
 * SimulationConfig} instances are passed the configuration parameters to construct the {@link
 * SimulationSpecImpl} objects. Once the {@link SimulationSpecImpl} is fully configured, the
 * instances thereof are ready to run which starts off the benchmark test.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see SimulationSpecImpl
 * @since 1.0
 */
public class SimulationConfig {

  private static final Logger LOG = LogManager.getLogger(SimulationConfig.class);
  private static final String PACKAGE_TO_SCAN = "packageToScan";
  private static final String SOURCE_CLASSPATH = "classpath://";
  private static SimulationConfig instance;

  private final Properties properties;
  private final String environment;

  private SimulationConfig(final String path, final Environment environment) {
    this.properties = new Properties();
    this.environment = environment.toString();

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
    // currently we do only support classpath configuration.
    var normPath = path.replace(SOURCE_CLASSPATH, "");
    var resourceAsStream = getClass().getResourceAsStream(normPath);

    try {
      properties.load(Optional.ofNullable(resourceAsStream)
          .orElseThrow(() -> new ConfigurationNotFoundException(
              "Properties file not found in path: " + path)));

      PropsValidatorImpl propsValidator = new PropsValidatorImpl();
      propsValidator.validate(properties);

    } catch (IOException e) {
      ExceptionUtils.rethrow(e, RhinoIOException.class, "Cannot read rhino.properties file.");
    }
  }

  private String getProperty(final String prop) {
    return properties.getProperty(prop);
  }

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

  private UserProvider.SourceType getUsersSource() {
    var source = properties.getProperty(environment + ".users.source", "file");
    return UserProvider.SourceType.valueOf(source.toUpperCase());
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

  String getPackageToScan() {
    return instance.getProperty(PACKAGE_TO_SCAN);
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

  public static String getVaultKey() {
    return instance.getAuthVaultKey();
  }
}
