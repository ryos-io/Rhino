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

import com.google.common.collect.ImmutableList;
import io.ryos.rhino.sdk.annotations.Before;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.DslBuilder;
import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.dsl.HttpRetriableDsl;
import io.ryos.rhino.sdk.dsl.SessionDslItem;
import io.ryos.rhino.sdk.dsl.data.HttpResponse;
import io.ryos.rhino.sdk.dsl.data.builder.MapperBuilder;
import io.ryos.rhino.sdk.dsl.mat.HttpDslData;
import io.ryos.rhino.sdk.providers.OAuthUserProvider;
import io.ryos.rhino.sdk.providers.UUIDProvider;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;

import static io.ryos.examples.benchmark.Constants.FILES_ENDPOINT;
import static io.ryos.examples.benchmark.Constants.X_REQUEST_ID;
import static io.ryos.rhino.sdk.dsl.DslBuilder.dsl;
import static io.ryos.rhino.sdk.dsl.data.UploadStream.file;
import static io.ryos.rhino.sdk.dsl.data.builder.ForEachBuilderImpl.in;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.http;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.some;
import static io.ryos.rhino.sdk.dsl.utils.HeaderUtils.headerValue;
import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.global;
import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.session;

@Simulation(name = "Upload and Get Simulation Multi-User")
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class UploadAndReadMultipleFilesWithMultiUsersSimulation {

  private static final ImmutableList<String> FILES_TO_UPLOAD = ImmutableList.of(
      "a/",
      "a/b/",
      "a/b/c/");

  @UserProvider
  private OAuthUserProvider userProvider;

  @Provider(clazz = UUIDProvider.class)
  private UUIDProvider uuidProvider;

  @Before
  public DslBuilder setUp() {
    return dsl()
        .session("files", FILES_TO_UPLOAD)
        .forEach(session("files"), this::uploadFile, "uploadedFiles")
        .forEach(session("files"), this::getMetadata, "metadata")
        .map(MapperBuilder.in(global("metadata"))
            .doMap(o -> ((HttpResponse)o).getResponseBodyAsString())
            .collect("ids", SessionDslItem.Scope.SIMULATION));
  }

  @Dsl(name = "Upload File")
  public DslBuilder simulateTwoUsersUploadDownload() {
    return dsl().
        forEach(global("ids"), o -> some("output").exec(s -> {
          System.out.println(o);
          return "OK";
        }));
  }

  private Object extractIds(HttpDslData r) {
    return r.getResponseBodyAsString();
  }

  private HttpDsl getMetadata(Object response) {
     return http("GET metadata")
        .header(c -> headerValue(X_REQUEST_ID, "TwoUsersUploadDownloadSimulationTest-" + uuidProvider.take()))
        .auth()
        .endpoint(session -> FILES_ENDPOINT + "/" + response)
        .get();
  }

  private HttpDsl uploadFile(Object file) {

    return http("File upload")
        .header(session -> headerValue(X_REQUEST_ID, "TwoUsersUploadDownloadSimulationTest-" + uuidProvider.take()))
        .auth()
        .endpoint(session -> FILES_ENDPOINT + "/" + file)
        .upload(() -> file("classpath:///test.txt"))
        .put();
  }

  private HttpDsl getFile(Object id) {
    return http("GET text.txt")
        .header(c -> headerValue(X_REQUEST_ID, "TwoUsersUploadDownloadSimulationTest-" + uuidProvider.take()))
        .auth(session -> userProvider.take())
        .endpoint(session("UPLOAD text.txt", "endpoint"))
        .get();
  }

}
