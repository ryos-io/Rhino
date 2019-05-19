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
import io.ryos.rhino.sdk.annotations.Feeder;
import io.ryos.rhino.sdk.annotations.Logging;
import io.ryos.rhino.sdk.annotations.Scenario;
import io.ryos.rhino.sdk.annotations.SessionFeeder;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserFeeder;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.feeders.UUIDFeeder;
import io.ryos.rhino.sdk.reporting.GatlingLogFormatter;
import io.ryos.rhino.sdk.reporting.Recorder;
import io.ryos.rhino.sdk.users.data.OAuthUser;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

@Simulation(name = "Server-Status Simulation")
@Logging(file = "/Users/bagdemir/sims/simulation.log", formatter = GatlingLogFormatter.class)
public class PerformanceTestingExample {

  @UserFeeder(max = 1, delay = 1000, factory = OAuthUserRepositoryFactoryImpl.class)
  private OAuthUser user;

  @SessionFeeder
  private UserSession userSession;

  @Feeder(factory = UUIDFeeder.class)
  private String uuid;

  @Before
  public void setUp() {
    System.out.println("Before the test with user:" + user.getUsername());
  }

  @Scenario(name = "Discovery")
  public void performDiscovery(Recorder recorder) {

    final Client client = ClientBuilder.newClient();
    final Response response = client
        .target("https://localhost:8080/")
        .request()
        .header("Authorization", "Bearer " + user.getAccessToken())
        .header("X-Request-Id", "Rhino-" + uuid)
        .get();

    System.out.println(Thread.currentThread().getName() + " - Discovery:"
        + user.getUsername()
        + " got  " + response.readEntity(String.class));

    recorder.record("Discovery API Call", response.getStatus());
  }

  @Scenario(name = "Health")
  public void performHealth(Recorder recorder) {

    final Client client = ClientBuilder.newClient();
    final Response response = client
        .target("https://localhost:8080")
        .request()
        .header("X-Request-Id", "Rhino-" + uuid)
        .get();

    System.out.println(Thread.currentThread().getName() + " - Health:"
        + user.getUsername()
        + " got  " + response.readEntity(String.class));

    recorder.record("Health API Call", response.getStatus());
  }

  @Scenario(name = "KO OK")
  public void performKO(Recorder recorder) {

    final Client client = ClientBuilder.newClient();
    final Response response = client
        .target("https://localhost:8080")
        .request()
        .get();

    System.out.println(Thread.currentThread().getName() + " - Fail:" + user.getUsername() + " got"
        + " " + response.readEntity(String.class));

    recorder.record("Broken Call", response.getStatus());
  }

  @After
  public void cleanUp() {
    // System.out.println("Clean up the test with user:" + user.getUsername());
  }
}
