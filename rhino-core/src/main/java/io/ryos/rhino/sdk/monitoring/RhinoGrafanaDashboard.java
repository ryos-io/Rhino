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

/**
 * Dashboard template generator takes the simulation metadata and generates a new dashboard from
 * the dashboard template given.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class RhinoGrafanaDashboard implements GrafanaDashboard {

  @Override
  public String getDashboard(final String simulationName,
      final String dashboardTemplate,
      final String[] dsls) {

    String dbTemplate = dashboardTemplate;

    dbTemplate = dbTemplate.replace("${SIMULATION_NAME}", simulationName);
    if (dsls != null && dsls.length > 0) {
      dbTemplate = dbTemplate.replace("${SCENARIO_1}", dsls[0]);
      // handle more than one scenario.
    }

    return dbTemplate;
  }
}
