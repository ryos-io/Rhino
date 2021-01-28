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
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.DslItem;
import io.ryos.rhino.sdk.monitoring.GrafanaGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Abstract runner contains common methods used in concrete sub-classes.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public abstract class AbstractSimulationRunner implements SimulationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractSimulationRunner.class);
  private static final String STOP_AFTER = "STOP_AFTER";

  private final SimulationMetadata simulationMetadata;

  public AbstractSimulationRunner(SimulationMetadata simulationMetadata) {
    this.simulationMetadata = simulationMetadata;
  }

  protected void setUpGrafanaDashboard() {
    LOG.info("Grafana is enabled. Creating dashboard: {}", SimulationConfig.getSimulationId());
    new GrafanaGateway(simulationMetadata.getGrafanaInfo())
        .setUpDashboard(SimulationConfig.getSimulationId(),
            simulationMetadata.getDslMethods()
                .stream()
                .map(DslItem::getName)
                .toArray(String[]::new));
  }

  protected int getStopAfterFromEnv() {
    var envVars = System.getenv();
    var numberOfTurns = envVars.get(STOP_AFTER);
    int stopAfter = -1;
    if (numberOfTurns != null) {
      stopAfter = Integer.parseInt(numberOfTurns);
    }
    return stopAfter;
  }

  protected Flux<UserSession> appendTake(Flux<UserSession> flux, int stopAfter) {
    if (stopAfter > 0) {
      flux = flux.take(stopAfter);
    } else {
      flux = flux.take(getSimulationMetadata().getDuration());
    }
    return flux;
  }

  public SimulationMetadata getSimulationMetadata() {
    return simulationMetadata;
  }
}
