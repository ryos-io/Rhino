/*
  Copyright 2018 Ryos.io.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package ${groupId}.benchmark;

import static io.ryos.rhino.sdk.dsl.DslBuilder.dsl;
import static io.ryos.rhino.sdk.dsl.MaterializableDslItem.http;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.resulting;
import static io.ryos.rhino.sdk.dsl.utils.HeaderUtils.headerValue;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.DslBuilder;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import java.util.UUID;

@Simulation(name = "Reactive Test")
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class ReactiveBasicHttpGetSimulation {

    private static final String FILES_ENDPOINT = "http://localhost:8089/files";
    private static final String X_REQUEST_ID = "X-Request-Id";
    private static final String X_API_KEY = "X-Api-Key";

    @Dsl(name = "Load DSL Request")
    public DslBuilder singleTestDsl() {
        return dsl()
            .run(http("Files Request")
                .header(session -> headerValue(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
                .header(X_API_KEY, SimulationConfig.getApiKey())
                .auth()
                .endpoint(FILES_ENDPOINT)
                .get()
                .saveTo("result"))
            .verify(http("Files Request 2")
                    .header(session -> headerValue(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
                    .header(X_API_KEY, SimulationConfig.getApiKey())
                    .auth()
                    .endpoint(FILES_ENDPOINT)
                    .get()
                    .saveTo("result"),
                resulting("200"));
    }
}

