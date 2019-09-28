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

package ${groupId}.benchmark;

import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Scenario;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.providers.UUIDProvider;
import io.ryos.rhino.sdk.reporting.Measurement;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Client;

@Simulation(name = "Server-Status Simulation")
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class RhinoEntity {

    private static final String TARGET = "http://localhost:8089/api/status";
    private static final String X_REQUEST_ID = "X-Request-Id";

    private Client client = ClientBuilder.newClient();

    @Provider(factory = UUIDProvider.class)
    private String uuid;

    @Scenario(name = "Health")
    public void performHealth(Measurement measurement) {

        var response = client
                .target(TARGET)
                .request()
                .header(X_REQUEST_ID, "Rhino-" + uuid)
                .get();

        measurement.measure("Health API Call", String.valueOf(response.getStatus()));
    }
}
