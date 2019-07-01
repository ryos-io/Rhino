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

package io.ryos.rhino.sdk;

import io.ryos.rhino.sdk.annotations.After;
import io.ryos.rhino.sdk.annotations.Before;
import io.ryos.rhino.sdk.annotations.CleanUp;
import io.ryos.rhino.sdk.annotations.Prepare;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Scenario;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.feeders.OAuthUserProvider;
import io.ryos.rhino.sdk.feeders.UUIDProvider;
import io.ryos.rhino.sdk.reporting.Measurement;
import io.ryos.rhino.sdk.users.data.OAuthUser;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

@Simulation(name = "Server-Status Simulation")
@UserRepository(max = 9, factory = OAuthUserRepositoryFactoryImpl.class)
public class BlockingJerseyClientLoadTestSimulation {

  @UserProvider(region = "US")
  private OAuthUserProvider userProviderUS;

  @UserProvider(region = "EU")
  private OAuthUserProvider userProviderEU;

  @Provider(factory = UUIDProvider.class)
  private String uuid;

  @Prepare
  public void prepare() {
    System.out.println("Preparation in progress.");
  }

  @CleanUp
  public void cleanUp() {
    System.out.println("Clean-up in progress.");
  }

  @Before
  public void setUp(UserSession us) {
    System.out.println("Before the test with user:" + us.getUser().getUsername());
  }

  @Scenario(name = "Discovery")
  public void performDiscovery(Measurement measurement, UserSession userSession) {

    OAuthUser user = userProviderUS.take();

    final Client client = ClientBuilder.newClient();
    final Response response = client
        .target("http://localhost:8089/api/files")
        .request()
        .header("Authorization", "Bearer " + user.getAccessToken())
        .header("X-Request-Id", "Rhino-" + uuid)
        .header("X-Api-Key", "CCStorage")
        .get();

    measurement.measure("Discovery API Call", String.valueOf(response.getStatus()));
  }

  @Scenario(name = "Health")
  public void performHealth(Measurement measurement, UserSession userSession) {

    final Client client = ClientBuilder.newClient();
    final Response response = client
        .target("http://localhost:8089/api/files")
        .request()
        .header("X-Request-Id", "Rhino-" + uuid)
        .get();

    measurement.measure("Health API Call", String.valueOf(response.getStatus()));
  }

  @Scenario(name = "KO OK")
  public void performKO(Measurement measurement, UserSession userSession) {

    final Client client = ClientBuilder.newClient();
    final Response response = client
        .target("http://localhost:8089/api/invalid")
        .request()
        .get();

    measurement.measure("Broken Call", String.valueOf(response.getStatus()));
  }

  @After
  public void after(UserSession us) {
    System.out.println("Clean up the test with user:" + us.getUser().getUsername());
  }
}
