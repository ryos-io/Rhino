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

package io.ryos.rhino.sdk;

import io.ryos.rhino.sdk.monitoring.GrafanaDashboard;

/**
 * Grafana integration entity.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class GrafanaInfo {

  private Class<? extends GrafanaDashboard> dashboard;
  private String pathToTemplate;

  GrafanaInfo(Class<? extends GrafanaDashboard> dashboard, String pathToTemplate) {
    this.dashboard = dashboard;
    this.pathToTemplate = pathToTemplate;
  }

  public Class<? extends GrafanaDashboard> getDashboard() {
    return dashboard;
  }

  public String getPathToTemplate() {
    return pathToTemplate;
  }
}
