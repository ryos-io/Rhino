/*
  Copyright 2018 Adobe.

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

package com.adobe.rhino.sdk;

import com.adobe.rhino.sdk.utils.Environment;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Simulation configuration instances are used to configure benchmark tests. The
 * {@link SimulationConfig} instances are passed the configuration parameters to construct
 * the {@link SimulationSpecImpl} objects. Once the {@link SimulationSpecImpl} is fully configured, the
 * instances thereof are ready to run which starts off the benchmark test.
 *
 * @author Erhan Bagdemir
 * @since 1.0
 * @see SimulationSpecImpl
 */
public class SimulationConfig {

  private static final String PACKAGE_TO_SCAN = "packageToScan";
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

  String getProperty(String prop) {
    return properties.getProperty(prop);
  }

  private void loadConfig(final String path) {
    // currently we do only support classpath configuration.
    final InputStream resourceAsStream = getClass().getResourceAsStream(path.replace("classpath://", ""));
    try {
      properties.load(resourceAsStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  String getDBSupportInfluxURL() {
    return properties.getProperty("db.influx.url");
  }

  String getDBSupportInfluxDBName() {
    return properties.getProperty("db.influx.dbName");
  }

  String getDBSupportInfluxUsername() {
    return properties.getProperty("db.influx.username");
  }

  String getDBSupportInfluxPassword() {
    return properties.getProperty("db.influx.password");
  }

  String getAuthUserSource() {
    return properties.getProperty(environment + ".auth.userSource");
  }

  String getAuthClientId() {
    return properties.getProperty(environment + ".auth.clientId");
  }

  String getAuthApiKey() {
    return properties.getProperty(environment + ".auth.apiKey");
  }

  String getAuthEndpoint() {
    return properties.getProperty(environment + ".auth.endpoint");
  }

  String getAuthClientSecret() {
    return properties.getProperty(environment + ".auth.clientSecret");
  }

  String getEndpoint() {
    return properties.getProperty(environment + ".endpoint");
  }

  String getAuthGrantType() {
    return properties.getProperty(environment + ".auth.grantType");
  }

  String getPackageToScan() {
    return instance.getProperty(PACKAGE_TO_SCAN);
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

  public static String getUserSource() {
    return instance.getAuthUserSource();
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
}
