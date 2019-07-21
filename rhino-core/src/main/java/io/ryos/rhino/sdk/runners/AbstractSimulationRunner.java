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

package io.ryos.rhino.sdk.runners;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.monitoring.GrafanaGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public abstract class AbstractSimulationRunner implements SimulationRunner {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSimulationRunner.class);

  private final SimulationMetadata simulationMetadata;

  public AbstractSimulationRunner(SimulationMetadata simulationMetadata) {
    this.simulationMetadata = simulationMetadata;
  }

  protected void setUpGrafanaDashboard() {
    LOG.info("Grafana is enabled. Creating dashboard: " + SimulationConfig.getSimulationId());
    new GrafanaGateway(simulationMetadata.getGrafanaInfo())
        .setUpDashboard(SimulationConfig.getSimulationId(),
            simulationMetadata.getScenarios()
                .stream()
                .map(Scenario::getDescription)
                .toArray(String[]::new));
  }

  public SimulationMetadata getSimulationMetadata() {
    return simulationMetadata;
  }
}
