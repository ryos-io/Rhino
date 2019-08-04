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

import static io.ryos.rhino.sdk.dsl.specs.HttpSpec.from;
import static io.ryos.rhino.sdk.dsl.specs.Spec.http;
import static io.ryos.rhino.sdk.dsl.specs.UploadStream.file;

import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.providers.OAuthUserProvider;
import io.ryos.rhino.sdk.providers.UUIDProvider;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;

/**
 */
@Simulation(name = "Reactive Monitor Test")
@Runner(clazz = ReactiveHttpSimulationRunner.class)
public class ReactiveMonitorWaitSimulation {

  private static final String FILES_ENDPOINT = "http://localhost:8089/api/files";
  private static final String MONITOR_ENDPOINT = "http://localhost:8089/api/monitor";
  private static final String X_REQUEST_ID = "X-Request-Id";
  private static final String X_API_KEY = "X-Api-Key";

  @UserProvider(region = "US")
  private OAuthUserProvider userProvider;

  @Provider(factory = UUIDProvider.class)
  private UUIDProvider uuidProvider;

  @Dsl(name = "Upload File")
  public LoadDsl singleTestDsl() {
    return Start
        .dsl()
        .run(http("Upload")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + uuidProvider.take()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .upload(() -> file("classpath:///test.txt"))
            .endpoint((c) -> FILES_ENDPOINT)
            .put()
            .saveTo("result"))
        .run(http("Monitor")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + uuidProvider.take()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint((c) -> MONITOR_ENDPOINT)
            .get()
            .saveTo("result")
            .retryIf((httpResponse) -> httpResponse.getStatusCode() != 200, 2));
  }
}
