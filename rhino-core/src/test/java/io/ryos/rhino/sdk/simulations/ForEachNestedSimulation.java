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

import static io.ryos.rhino.sdk.dsl.DslBuilder.dsl;
import static io.ryos.rhino.sdk.dsl.MaterializableDslItem.http;
import static io.ryos.rhino.sdk.dsl.data.builder.ForEachBuilderImpl.in;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.collect;
import static io.ryos.rhino.sdk.dsl.utils.HeaderUtils.headerValue;
import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.session;
import static io.ryos.rhino.sdk.utils.TestUtils.getEndpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.RampUp;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.DslBuilder;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import java.util.UUID;

@Simulation(name = "Reactive Multi-User Test", durationInMins = 1)
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
@RampUp(startRps = 1, targetRps = 100)
public class ForEachNestedSimulation {

  private static final String DISCOVERY_ENDPOINT = getEndpoint("discovery");
  private static final String X_REQUEST_ID = "X-Request-Id";
  private static final String X_API_KEY = "X-Api-Key";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Dsl(name = "Nested forEach DSL")
  public DslBuilder setUp() {
    return dsl()
        .session("i", () -> ImmutableList.of(1, 2, 3))
        .session("j", () -> ImmutableList.of(1, 2, 3))
        .forEach(in(session("i")).exec(i ->
            dsl()
                .run(http("Discovery Request -1")
                    .header(session -> headerValue(X_REQUEST_ID,
                        "Rhino-" + UUID.randomUUID().toString()))
                    .header(X_API_KEY, SimulationConfig.getApiKey())
                    .auth()
                    .endpoint(DISCOVERY_ENDPOINT)
                    .get())
                //                    .collect("list.outer")
                .forEach(in(session("j")).exec(j ->
                    dsl()
                        .run(collect(http("Discovery Request -2")
                            .header(session -> headerValue(X_REQUEST_ID,
                                "Rhino-" + UUID.randomUUID().toString()))
                            .header(X_API_KEY, SimulationConfig.getApiKey())
                            .auth()
                            .endpoint(DISCOVERY_ENDPOINT)
                            .get(), "list.inner"))
                ))));
  }

  private DslBuilder getDiscovery() {

    return dsl()
        .run(http("Discovery Request")
            .header(session -> headerValue(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(DISCOVERY_ENDPOINT)
            .get()
            .saveTo("result"));
  }
}
