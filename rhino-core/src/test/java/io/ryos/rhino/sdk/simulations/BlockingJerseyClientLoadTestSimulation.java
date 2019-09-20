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

package io.ryos.rhino.sdk.simulations;

import io.ryos.rhino.sdk.annotations.CleanUp;
import io.ryos.rhino.sdk.annotations.Prepare;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Scenario;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.data.SimulationSession;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.providers.UUIDProvider;
import io.ryos.rhino.sdk.reporting.Measurement;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.oauth.OAuthService;
import io.ryos.rhino.sdk.users.oauth.OAuthUser;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@Simulation(name = "Server-Status Simulation")
@UserRepository(factory = OAuthUserRepositoryFactory.class)
public class BlockingJerseyClientLoadTestSimulation {

  private static final String TARGET = "http://localhost:8089/api/status";
  private static final String X_REQUEST_ID = "X-Request-Id";

  private Client client = ClientBuilder.newClient();

  @Provider(factory = UUIDProvider.class)
  private String uuid;

  @Prepare
  public static void prepare(SimulationSession session) {
    System.out.println("Prepare");

    session.add("number", 1);
  }

  @Scenario(name = "Discovery API")
  public void performHealth(Measurement measurement, UserSession session) {

    User user = session.getUser();
    OAuthUser oauthUser = null;
    if (user instanceof OAuthUser) {
      oauthUser = ((OAuthUser) session.getUser());
    }

    assert oauthUser != null;

    String serviceAccessToken = null;
    if (oauthUser.getOAuthService() != null) {
      serviceAccessToken = oauthUser.getOAuthService().getAccessToken();
    }

    var response = client
        .target(TARGET)
        .request()
        .header(X_REQUEST_ID, "Rhino-" + uuid)
        .header("Authorization", "Bearer " + serviceAccessToken)
        .header("X-User-Token", oauthUser.getAccessToken())
        .get();

    measurement.measure("Discovery API Call", String.valueOf(response.getStatus()));
  }

  @CleanUp
  public static void clean(SimulationSession session) {
    System.out.println("cleanup");

    System.out.println(session.get("number"));
  }
}
