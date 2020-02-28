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

package io.ryos.examples.simulations;

import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.providers.OAuthUserProvider;
import io.ryos.rhino.sdk.providers.UUIDProvider;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;

import static io.ryos.examples.benchmark.Constants.DISCOVERY_ENDPOINT;
import static io.ryos.examples.benchmark.Constants.X_REQUEST_ID;
import static io.ryos.rhino.sdk.dsl.LoadDsl.dsl;
import static io.ryos.rhino.sdk.dsl.data.UploadStream.file;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.http;
import static io.ryos.rhino.sdk.dsl.utils.HeaderUtils.headerValue;

@Simulation(name = "Upload and Get Simulation")
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class MultipleUserSimulation {

  @UserProvider
  private OAuthUserProvider userProvider;

  @Provider(clazz = UUIDProvider.class)
  private UUIDProvider uuidProvider;

  @Dsl(name = "Upload File")
  public LoadDsl singleTestDsl() {
    return dsl()
        .run(http("UPLOAD text.txt")
            .header(c -> headerValue(X_REQUEST_ID, "Rhino-" + uuidProvider.take()))
            .auth()
            .endpoint((c) -> DISCOVERY_ENDPOINT)
            .upload(() -> file("classpath:///test.txt"))
            .put()
            .saveTo("result"))
        .run(http("GET text.txt")
            .header(c -> headerValue(X_REQUEST_ID, "Rhino-" + uuidProvider.take()))
            .auth(userProvider.take())
            .endpoint((c) -> DISCOVERY_ENDPOINT)
            .upload(() -> file("classpath:///test.txt"))
            .put()
            .saveTo("result"));
  }
}
