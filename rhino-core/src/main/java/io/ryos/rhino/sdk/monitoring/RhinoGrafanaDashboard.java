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

import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

/**
 * TODO
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class RhinoGrafanaDashboard implements GrafanaDashboard {

  @Override
  public String getDashboard(final String simulationName,
      final String dashboardTemplate,
      final String[] scenarios) {

    String dbTemplate = dashboardTemplate;

    dbTemplate = dbTemplate.replace("${SIMULATION_NAME}", simulationName);
    if (scenarios != null && scenarios.length > 0) {
      dbTemplate = dbTemplate.replace("${SCENARIO_1}", scenarios[0]);
      // handle more than one scenario.
    }

    return dbTemplate;
  }
}
