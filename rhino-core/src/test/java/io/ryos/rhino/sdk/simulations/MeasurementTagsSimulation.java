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
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.run;
import static io.ryos.rhino.sdk.dsl.utils.HeaderUtils.headerValue;
import static io.ryos.rhino.sdk.utils.TestUtils.getEndpoint;

import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.DslBuilder;
import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.providers.UUIDProvider;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;

@Simulation(name = "Nested Measurement Test")
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class MeasurementTagsSimulation {

  private static final String DISCOVERY_ENDPOINT = getEndpoint("discovery");
  private static final String FILE_ENDPOINT = getEndpoint("files");

  @Provider(clazz = UUIDProvider.class)
  private UUIDProvider provider;

  @Dsl(name = "Load DSL Discovery and GET")
  public DslBuilder loadTestDiscoverAndGet() {
    return dsl()
        .measure("Outer Measurement",
            dsl().run(discovery())
                .measure("Inner Measurement",
                    run(getResource())));
  }

  private HttpDsl getResource() {
    return http("Get Request")
        .auth()
        .header(session -> headerValue("X-Request-Id", "Test-" + provider.take()))
        .endpoint(s -> FILE_ENDPOINT)
        .get();
  }

  private HttpDsl discovery() {
    return http("Discovery Request")
        .auth()
        .header(session -> headerValue("X-Request-Id", "Test-" + provider.take()))
        .endpoint(DISCOVERY_ENDPOINT)
        .get()
        .saveTo("result");
  }
}
