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
import io.ryos.rhino.sdk.dsl.HttpRetriableDsl;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.providers.OAuthUserProvider;
import io.ryos.rhino.sdk.providers.UUIDProvider;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;

import static io.ryos.examples.benchmark.Constants.FILES_ENDPOINT;
import static io.ryos.examples.benchmark.Constants.X_REQUEST_ID;
import static io.ryos.rhino.sdk.dsl.LoadDsl.dsl;
import static io.ryos.rhino.sdk.dsl.data.UploadStream.file;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.http;
import static io.ryos.rhino.sdk.dsl.utils.HeaderUtils.headerValue;
import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.session;

/*
 Every simulation needs to start with a @Simulation annotation and a describing name which is used
 in reporting and dashboards. The simulations, that require user interactions as in "a user is
 sending a request to the backend", must include a user repository factory, that is a factory for
 user repositories. User repositories are sources for users.
 */
@Simulation(name = "Upload and Get Simulation")
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class TwoUsersUploadDownloadSimulation {

  @UserProvider
  private OAuthUserProvider userProvider;

  @Provider(clazz = UUIDProvider.class)
  private UUIDProvider uuidProvider;

  @Dsl(name = "Upload File")
  public LoadDsl simulateTwoUsersUploadDownload() {
    return dsl()
        .run(putFile())
        .run(getFile());
  }

  private HttpRetriableDsl getFile() {
    return http(  "GET text.txt")
        .header(c -> headerValue(X_REQUEST_ID, "TwoUsersUploadDownloadSimulationTest-" + uuidProvider.take()))
        .auth(session -> userProvider.take())
        .endpoint(session("UPLOAD text.txt", "endpoint"))
        .get();
  }

  private HttpRetriableDsl putFile() {
    return http("UPLOAD text.txt")
        .header(session -> headerValue(X_REQUEST_ID, "TwoUsersUploadDownloadSimulationTest-" + uuidProvider.take()))
        .auth()
        .endpoint(session -> FILES_ENDPOINT + "/" + uuidProvider.take())
        .upload(() -> file("classpath:///test.txt"))
        .put();
  }
}
