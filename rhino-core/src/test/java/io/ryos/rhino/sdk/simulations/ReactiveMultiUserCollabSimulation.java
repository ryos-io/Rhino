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

package io.ryos.rhino.sdk.simulations;

import static io.ryos.rhino.sdk.dsl.DslBuilder.dsl;
import static io.ryos.rhino.sdk.dsl.MaterializableDslItem.http;
import static io.ryos.rhino.sdk.dsl.data.UploadStream.file;
import static io.ryos.rhino.sdk.dsl.data.builder.ForEachBuilderImpl.in;
import static io.ryos.rhino.sdk.dsl.utils.HeaderUtils.headerValue;
import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.global;
import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.session;
import static io.ryos.rhino.sdk.utils.TestUtils.getEndpoint;

import com.google.common.collect.ImmutableList;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.annotations.Before;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.DslBuilder;
import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.dsl.HttpRetriableDsl;
import io.ryos.rhino.sdk.dsl.SessionDslItem.Scope;
import io.ryos.rhino.sdk.providers.OAuthUserProvider;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import java.util.List;

@Simulation(name = "Reactive Multi-User Test")
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class ReactiveMultiUserCollabSimulation {

  private static final String X_REQUEST_ID = "X-Request-Id";
  private static final String X_API_KEY = "X-Api-Key";
  private static final String FILES_ENDPOINT = getEndpoint("files");

  @UserProvider
  private OAuthUserProvider userProvider;

  static List<String> getFiles() {
    return ImmutableList.of("file1", "file2");
  }

  @Before
  public DslBuilder setUp() {
    return dsl()
        .run(uploadFile())
        .session("files", ReactiveMultiUserCollabSimulation::getFiles)
        .forEach("test for each",
            in(session("files"))
                .exec(this::uploadFileForSecondUser)
                .collect("uploads", Scope.SIMULATION));
  }

  private HttpRetriableDsl uploadFileForSecondUser(Object file) {
    return http("PUT in Loop")
        .header(X_API_KEY, SimulationConfig.getApiKey())
        .auth()
        .endpoint(session -> FILES_ENDPOINT + "/" + file)
        .upload(() -> file("classpath:///test.txt"))
        .put();
  }

  private HttpDsl uploadFile() {
    return http("Prepare by PUT text.txt")
        .header(session -> headerValue(X_REQUEST_ID, "Rhino-" + userProvider.take()))
        .header(X_API_KEY, SimulationConfig.getApiKey())
        .auth()
        .endpoint(session -> FILES_ENDPOINT)
        .upload(() -> file("classpath:///test.txt"))
        .put().saveTo("Prepare by PUT text.txt", Scope.SIMULATION);
  }

  @Dsl(name = "Upload and Get")
  public DslBuilder loadTestPutAndGetFile() {
    return dsl()
        .forEach("get all files", in(global("uploads")).exec(file ->
            http("GET in Loop")
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(session -> FILES_ENDPOINT)
            .get()))
        .session("userB", () -> userProvider.take())
        .run(http("PUT text.txt")
            .header(session -> headerValue(X_REQUEST_ID, "Rhino-" + userProvider.take()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(session -> FILES_ENDPOINT)
            .upload(() -> file("classpath:///test.txt"))
            .put())
        .run(http("GET text.txt")
            .header(session -> headerValue(X_REQUEST_ID, "Rhino-" + userProvider.take()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth(session("userB"))
            .endpoint(global("Prepare by PUT text.txt", "endpoint"))
            .get());
  }
}
