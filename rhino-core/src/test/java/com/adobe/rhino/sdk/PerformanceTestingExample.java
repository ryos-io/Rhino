/*
  Copyright 2018 Adobe.

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

package com.adobe.rhino.sdk;

import com.adobe.rhino.sdk.annotations.After;
import com.adobe.rhino.sdk.annotations.Before;
import com.adobe.rhino.sdk.annotations.Feeder;
import com.adobe.rhino.sdk.annotations.Logging;
import com.adobe.rhino.sdk.annotations.Scenario;
import com.adobe.rhino.sdk.annotations.SessionFeeder;
import com.adobe.rhino.sdk.annotations.Simulation;
import com.adobe.rhino.sdk.annotations.UserFeeder;
import com.adobe.rhino.sdk.data.UserSession;
import com.adobe.rhino.sdk.feeders.UUIDFeeder;
import com.adobe.rhino.sdk.reporting.GatlingLogFormatter;
import com.adobe.rhino.sdk.users.IMSUserRepositoryFactoryImpl;
import com.adobe.rhino.sdk.users.OAuthUser;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

@Simulation(name = "Server-Status Simulation")
@Logging(file = "/Users/bagdemir/sims/simulation.log", formatter = GatlingLogFormatter.class)
public class PerformanceTestingExample {

  @UserFeeder(max = 1, delay = 1000, factory = IMSUserRepositoryFactoryImpl.class)
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
        .target("https://cc-api-storage-stage.adobe.io/")
        .request()
        .header("Authorization", "Bearer " + user.getAccessToken())
        .header("X-Request-Id", "Rhino-" + uuid)
        .header("X-API-Key", "CCStorage")
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
        .target("https://cc-api-storage-stage.adobe.io/server-status/health")
        .request()
        .header("X-Request-Id", "Rhino-" + uuid)
        .header("X-API-Key", "CCStorage")
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
        .target("https://cc-api-storage-stage.adobe.io/")
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
