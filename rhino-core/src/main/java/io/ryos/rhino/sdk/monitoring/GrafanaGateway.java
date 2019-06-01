/*
 * Copyright 2018 Ryos.io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ryos.rhino.sdk.monitoring;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.stream.Collectors;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

/**
 * Gateway implementation for Grafana.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class GrafanaGateway {

  private static final String GRAFANA_DASHBOARD_TEMPLATE = "/grafana/dashboard.json";
  private static final String TARGET = SimulationConfig.getGrafanaEndpoint() + "/api/dashboards/db";
  private static final String HEADER_AUTHORIZATION = "Authorization";

  /**
   * Creates a new dashboard for the simulation.
   * <p>
   */
  public void setUpDashboard(final String simulationName, final String... scenarios) {
    var resourceAsStream = getClass().getResourceAsStream(GRAFANA_DASHBOARD_TEMPLATE);

    if (resourceAsStream != null) {

      var dbTemplate = getDashboardCode(simulationName, resourceAsStream, scenarios);
      var request = ClientBuilder.newClient()
          .target(getUri())
          .request();
      var createDashboardResponse = request
          .header(HEADER_AUTHORIZATION, "Bearer " + getStrippedToken())
          .post(Entity.entity(dbTemplate, MediaType.APPLICATION_JSON));
      if (createDashboardResponse.getStatus() != Status.OK.getStatusCode() &&
          createDashboardResponse.getStatus() != Status.PRECONDITION_FAILED.getStatusCode()) {
        throw new GrafanaSetupException(
            "Server response was : " + createDashboardResponse.getStatus());
      }
    }
  }

  private String getStrippedToken() {
    return SimulationConfig.getGrafanaToken()
        .substring(1, SimulationConfig.getGrafanaToken().length() - 1);
  }

  private String getDashboardCode(final String simulationName,
      final InputStream resourceAsStream,
      final String[] scenarios) {

    String dbTemplate = "";
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(resourceAsStream, Charset.defaultCharset()))) {

      dbTemplate = br.lines().collect(Collectors.joining(System.lineSeparator()));
      dbTemplate = dbTemplate.replace("${SIMULATION_NAME}", simulationName);
      if (scenarios != null && scenarios.length > 0) {
        dbTemplate = dbTemplate.replace("${SCENARIO_1}", scenarios[0]);
        // handle more than one scenario.
      }

    } catch (IOException ioe) {
      ExceptionUtils.rethrow(ioe, GrafanaSetupException.class, "Cannot read Grafana dashboard"
          + " template.");
    }
    return dbTemplate;
  }

  private URI getUri() {
    try {
      return new URI(TARGET);
    } catch (URISyntaxException e) {
      ExceptionUtils.rethrow(e, GrafanaSetupException.class);
    }
    return null;
  }
}
