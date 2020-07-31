/*
 * Copyright 2020 Ryos.io.
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

package io.ryos.examples.simulations;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.DslBuilder;
import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.dsl.HttpRetriableDsl;

import io.ryos.rhino.sdk.dsl.data.HttpResponse;
import io.ryos.rhino.sdk.dsl.mat.HttpDslData;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import static io.ryos.examples.benchmark.Constants.DISCOVERY_ENDPOINT;
import static io.ryos.examples.benchmark.Constants.MAPPER;
import static io.ryos.examples.benchmark.Constants.X_API_KEY;
import static io.ryos.examples.benchmark.Constants.X_REQUEST_ID;
import static io.ryos.rhino.sdk.dsl.DslBuilder.dsl;
import static io.ryos.rhino.sdk.dsl.MaterializableDslItem.http;
import static io.ryos.rhino.sdk.dsl.data.builder.MapperBuilder.from;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.run;
import static io.ryos.rhino.sdk.dsl.utils.HeaderUtils.headerValue;
import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.session;

@Simulation(name = "Reactive Multi-User Test")
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class ExtractInformationWithMapSimulation {

  @Dsl(name = "Load DSL Discovery and GET")
  public DslBuilder simulateExtractInformationWithMap() {
    return dsl().measure("measure 1",
        run(getDiscovery())
            .map(from("result")
                .doMap(result -> extractEndpoint((HttpDslData) result))
                .saveTo("endpoint"))
            .run(get()));
  }

  private HttpRetriableDsl get() {
    return http("Get Request")
        .header(session -> headerValue(X_REQUEST_ID, "TwoUsersUploadDownloadSimulationTest-" + UUID.randomUUID().toString()))
        .header(X_API_KEY, SimulationConfig.getApiKey())
        .auth()
        .endpoint(session("endpoint"))
        .get();
  }

  private HttpDsl getDiscovery() {
    return http("Discovery Request")
        .header(session -> headerValue(X_REQUEST_ID, "TwoUsersUploadDownloadSimulationTest-" + UUID.randomUUID().toString()))
        .header(X_API_KEY, SimulationConfig.getApiKey())
        .auth()
        .endpoint(DISCOVERY_ENDPOINT)
        .get()
        .saveTo("result");
  }

  private String extractEndpoint(HttpDslData result) {
    try {
      HttpResponse response = result.getResponse();
      JsonNode jsonNode = MAPPER.readTree(response.getResponse().getResponseBody());
      return jsonNode.get("endpoint").asText();
    } catch (IOException e) {
      e.printStackTrace();
    }

    throw new RuntimeException();
  }
}
