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

import static io.ryos.rhino.sdk.specs.HttpSpec.from;
import static io.ryos.rhino.sdk.specs.Spec.http;
import static io.ryos.rhino.sdk.specs.UploadStream.file;

import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Prepare;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.providers.UUIDProvider;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactory;
import java.util.UUID;

@Simulation(name = "Reactive Async Wait Test", durationInMins = 1)
@Runner(clazz = ReactiveHttpSimulationRunner.class)
@UserRepository(factory = OAuthUserRepositoryFactory.class)
public class ReactiveWaitForMonitoringSimulation {
  private static final String FILES_ENDPOINT = "http://localhost:8089/api/files";
  private static final String MONITOR_ENDPOINT = "http://localhost:8089/api/monitor";
  private static final String X_REQUEST_ID = "X-Request-Id";
  private static final String X_API_KEY = "X-Api-Key";

  @Provider(factory = UUIDProvider.class)
  private UUIDProvider provider;

  @Prepare


  @Dsl(name = "Upload Hierarchy")
  public LoadDsl singleTestDsl() {
    return Start.dsl()
        .run(http("Upload")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(FILES_ENDPOINT)
            .upload(() -> file("classpath:///test.txt"))
            .put()
            .saveTo("result"))
        .run(http("Monitor")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(MONITOR_ENDPOINT)
            .get()
            .saveTo("result")
            .retryIf((r) -> r.getStatusCode() == 200, 10));
  }
}
