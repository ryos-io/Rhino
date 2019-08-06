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

import io.ryos.rhino.sdk.GrafanaInfo;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import io.ryos.rhino.sdk.exceptions.RhinoIOException;
import io.ryos.rhino.sdk.io.ConfigResource;
import io.ryos.rhino.sdk.utils.ReflectionUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
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
  private static final String TARGET = SimulationConfig.getGrafanaEndpoint() + "/api/dashboards/db";
  private static final String HEADER_AUTHORIZATION = "Authorization";

  private final GrafanaInfo grafanaInfo;

  public GrafanaGateway(GrafanaInfo grafanaInfo) {
    this.grafanaInfo = Objects.requireNonNull(grafanaInfo);
  }

  /**
   * Creates a new dashboard for the simulation.
   * <p>
   */
  public void setUpDashboard(final String simulationName, final String... scenarios) {

    var dbTemplate = getDashboardJson(simulationName, scenarios);
    var request = ClientBuilder.newClient()
        .target(getUri())
        .request();

    if (getStrippedToken() != null) {
      request = request.header(HEADER_AUTHORIZATION, "Bearer " + getStrippedToken());
    }
    var createDashboardResponse = request
        .post(Entity.entity(dbTemplate, MediaType.APPLICATION_JSON));

    if (createDashboardResponse.getStatus() != Status.OK.getStatusCode() &&
        createDashboardResponse.getStatus() != Status.PRECONDITION_FAILED.getStatusCode()) {
      throw new GrafanaSetupException(
          "Server response was : " + createDashboardResponse.getStatus());
    }
  }

  private String getDashboardJson(String simulationName, String[] scenarios) {
    var configResource = new ConfigResource(grafanaInfo.getPathToTemplate());
    try (var reader = new BufferedReader(new InputStreamReader(configResource.getInputStream()))) {
      var template = reader.lines().collect(Collectors.joining("\n"));
      Optional<? extends GrafanaDashboard> grafanaDashboard = ReflectionUtils
          .instanceOf(grafanaInfo.getDashboard());
      return grafanaDashboard.map(d -> d.getDashboard(simulationName, template, scenarios))
          .orElseThrow(IllegalArgumentException::new);
    } catch (IOException e) {
      throw new RhinoIOException("Cannot create dashboard.");
    }
  }

  private String getStrippedToken() {
    var grafanaToken = SimulationConfig.getGrafanaToken();
    if (grafanaToken == null || grafanaToken.length() == 0) {
      return null;
    }
    if (grafanaToken.length() < 2) {
      throw new IllegalArgumentException("Grafana token is invalid.");
    }
    return SimulationConfig.getGrafanaToken()
        .substring(1, SimulationConfig.getGrafanaToken().length() - 1);
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
