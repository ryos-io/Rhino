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
import static io.ryos.rhino.sdk.dsl.MaterializableDslItem.some;
import static io.ryos.rhino.sdk.dsl.data.UploadStream.file;
import static io.ryos.rhino.sdk.dsl.data.builder.ForEachBuilderImpl.in;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.runIf;
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
import io.ryos.rhino.sdk.dsl.SessionDslItem.Scope;
import io.ryos.rhino.sdk.providers.OAuthUserProvider;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import java.util.List;

@Simulation(name = "Reactive Multi-User Test")
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class ForEachUploadSimulation {

  private static final String FILES_ENDPOINT = getEndpoint("files");
  private static final String X_API_KEY = "X-Api-Key";

  @UserProvider
  private OAuthUserProvider userProvider;

  static List<String> getFiles() {
    return ImmutableList.of("file1", "file2");
  }

  @Before
  public DslBuilder setUp() {
    return dsl()
        .session("index", () -> ImmutableList.of(1, 2, 3))
        .forEach("iterate",
            in(ImmutableList.of(1, 2, 3)).exec(index -> some("count").exec(s -> {
              System.out.println(index);
              return "OK";
            })))
        .forEach("upload loop", in(session("index")).exec(index ->
                http("Prepare by PUT text.txt")
                .header(X_API_KEY, SimulationConfig.getApiKey())
                .auth()
                .endpoint(session -> FILES_ENDPOINT + "/" + index)
                .upload(() -> file("classpath:///test.txt"))
                    .get()).collect("uploads", Scope.SIMULATION));
  }

  @Dsl(name = "Get")
  public DslBuilder loadTestPutAndGetFile() {
    return dsl()
        .forEach("get files",
            in(global("uploads")).exec(index -> runIf(s -> true,
                http("GET text.txt")
                    .header(X_API_KEY, SimulationConfig.getApiKey())
                    .auth()
                    .endpoint(session -> FILES_ENDPOINT + "/" + index)
                    .get())).collect("uploads"));
  }
}
