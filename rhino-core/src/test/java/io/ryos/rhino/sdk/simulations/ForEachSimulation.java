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

import static io.ryos.rhino.sdk.dsl.specs.Spec.http;
import static io.ryos.rhino.sdk.dsl.specs.UploadStream.file;
import static io.ryos.rhino.sdk.dsl.specs.builder.ForEachBuilderImpl.in;

import com.google.common.collect.ImmutableList;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.annotations.Before;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.providers.OAuthUserProvider;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import java.util.List;

@Simulation(name = "Reactive Multi-User Test")
@Runner(clazz = ReactiveHttpSimulationRunner.class)
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class ForEachSimulation {

  private static final String FILES_ENDPOINT = "http://localhost:8089/api/files";
  private static final String X_REQUEST_ID = "X-Request-Id";
  private static final String X_API_KEY = "X-Api-Key";

  @UserProvider
  private OAuthUserProvider userProvider;

  static List<String> getFiles() {
    return ImmutableList.of("file1", "file2");
  }

  @Before
  public LoadDsl setUp() {
    return Start.dsl()
        .session("files", ReactiveMultiUserCollabSimulation::getFiles)
        .forEach("forEach",
            in("files").doRun(file -> http("Prepare by PUT text.txt")
                .header(X_API_KEY, SimulationConfig.getApiKey())
                .auth()
                .endpoint(session -> FILES_ENDPOINT + "/" + file)
                .upload(() -> file("classpath:///test.txt"))
                .put()).saveTo("uploads"));
  }

  @Dsl(name = "Upload and Get")
  public LoadDsl loadTestPutAndGetFile() {
    return Start.dsl()
        .forEach("upload loop",
            in("files").doRun(file ->
                http("Prepare by PUT text.txt")
                    .header(X_API_KEY, SimulationConfig.getApiKey())
                    .auth()
                    .endpoint(session -> FILES_ENDPOINT + "/" + file)
                    .upload(() -> file("classpath:///test.txt"))
                    .get()).saveTo("uploads"));
  }

}
