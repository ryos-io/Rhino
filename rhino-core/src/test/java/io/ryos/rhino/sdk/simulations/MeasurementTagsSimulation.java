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

package io.ryos.rhino.sdk.simulations;

import static io.ryos.rhino.sdk.dsl.HttpDsl.from;
import static io.ryos.rhino.sdk.dsl.LoadDsl.dsl;
import static io.ryos.rhino.sdk.dsl.MaterializableDslItem.http;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.run;
import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.session;
import static io.ryos.rhino.sdk.utils.TestUtils.getEndpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.dsl.HttpRetriableDsl;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.data.HttpResponse;
import io.ryos.rhino.sdk.dsl.data.builder.MapperBuilder;
import io.ryos.rhino.sdk.dsl.mat.HttpDslData;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import java.io.IOException;
import java.util.UUID;

@Simulation(name = "Reactive Multi-User Test")
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class MeasurementTagsSimulation {

  private static final String DISCOVERY_ENDPOINT = getEndpoint("discovery");
  private static final String X_REQUEST_ID = "X-Request-Id";
  private static final String X_API_KEY = "X-Api-Key";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Dsl(name = "Load DSL Discovery and GET")
  public LoadDsl loadTestDiscoverAndGet() {
    return dsl().measure("level1",
        run(discovery())
            .map(MapperBuilder.from("result").doMap(result -> extractEndpoint((HttpDslData) result))
                .saveTo("endpoint"))
            .measure("level2", run(getResource())));
  }

  private HttpRetriableDsl getResource() {
    return http("Get Request")
        .header(session -> from(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
        .header(X_API_KEY, SimulationConfig.getApiKey())
        .auth()
        .endpoint(session("endpoint"))
        .get();
  }

  private HttpDsl discovery() {
    return http("Discovery Request")
        .header(session -> from(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
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
