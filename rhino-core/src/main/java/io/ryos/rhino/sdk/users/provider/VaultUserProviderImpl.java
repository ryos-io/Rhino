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

package io.ryos.rhino.sdk.users.provider;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import io.ryos.rhino.sdk.users.UserParser;
import io.ryos.rhino.sdk.users.VaultUserParserImpl;
import io.ryos.rhino.sdk.users.data.User;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Vault implementation of {@link UserProvider}.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class VaultUserProviderImpl implements UserProvider {
  private static final String PATH_ROOT_CONTEXT = "v1/secret/data";
  private static final String X_VAULT_TOKEN = "X-Vault-Token";
  private static final String VAULT_TOKEN = "VAULT_TOKEN";

  private final UserParser userParser = new VaultUserParserImpl();

  @Override
  public List<User> getUsers() {
    var uri = getVaultURI();
    var token = Optional
        .ofNullable(SimulationConfig.getVaultToken())
        .orElseGet(() -> System.getProperty(VAULT_TOKEN));

    if (token == null) {
      throw new VaultTokenNotFoundException("<env>.auth.vault.token is missing in rhino"
          + ".properties.");
    }

    var client = ClientBuilder.newClient();
    var response = client.target(uri)
        .request()
        .header(X_VAULT_TOKEN, token)
        .get();

    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      return userParser.unmarshal(response.readEntity(InputStream.class));
    }

    return Collections.emptyList();
  }

  private URI getVaultURI() {
    URI uri = null;

    try {
      uri = UriBuilder.fromUri(new URI(SimulationConfig.getVaultEndpoint()))
          .path(PATH_ROOT_CONTEXT)
          .path(SimulationConfig.getVaultPath())
          .build();
    } catch (URISyntaxException e) {
      ExceptionUtils.rethrow(e, RuntimeException.class, "<env>.auth.vault.endpoint is missing in "
          + "rhino.properties.");
    }
    return uri;
  }
}
