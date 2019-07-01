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
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Logging;
import io.ryos.rhino.sdk.annotations.Scenario;
import io.ryos.rhino.sdk.annotations.SessionFeeder;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.feeders.UUIDProvider;
import io.ryos.rhino.sdk.reporting.GatlingLogFormatter;
import io.ryos.rhino.sdk.reporting.Measurement;
import io.ryos.rhino.sdk.users.data.OAuthUser;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

@Simulation(name = "Server-Status Simulation")
@Logging(file = "/Users/bagdemir/sims/simulation.log", formatter = GatlingLogFormatter.class)
@UserRepository(max=2, factory = OAuthUserRepositoryFactoryImpl.class)
public class BlockingJerseyClientLoadTestSimulation {

  @UserProvider
  private OAuthUser user;

  @Provider(factory = UUIDProvider.class)
  private String uuid;

  @Before
  public void setUp() {
    // System.out.println("Before the test with user:" + user.getUsername());
  }

  @Scenario(name = "Discovery")
  public void performDiscovery(Measurement measurement) {

    final Client client = ClientBuilder.newClient();
    final Response response = client
        .target("https://cc-api-storage-stage.adobe.io/")
        .request()
        .header("Authorization", "Bearer " + user.getAccessToken())
        .header("X-Request-Id", "Rhino-" + uuid)
        .header("X-Api-Key", "CCStorage")
        .get();

    measurement.measure("Discovery API Call", String.valueOf(response.getStatus()));
  }

  @Scenario(name = "Health")
  public void performHealth(Measurement measurement) {

    final Client client = ClientBuilder.newClient();
    final Response response = client
        .target("https://cc-api-storage-stage.adobe.io/")
        .request()
        .header("X-Request-Id", "Rhino-" + uuid)
        .get();

    measurement.measure("Health API Call", String.valueOf(response.getStatus()));
  }

  @Scenario(name = "KO OK")
  public void performKO(Measurement measurement) {

    final Client client = ClientBuilder.newClient();
    final Response response = client
        .target("https://cc-api-storage-stage.adobe.io/")
        .request()
        .get();

    measurement.measure("Broken Call", String.valueOf(response.getStatus()));
  }

  @After
  public void cleanUp() {
    // System.out.println("Clean up the test with user:" + user.getUsername());
  }
}
