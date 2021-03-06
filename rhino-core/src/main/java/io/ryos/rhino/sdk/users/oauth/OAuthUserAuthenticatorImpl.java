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

package io.ryos.rhino.sdk.users.oauth;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import io.ryos.rhino.sdk.exceptions.UserLoginException;
import io.ryos.rhino.sdk.users.data.User;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link UserAuthenticator} implementation for OAuth 2.0.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class OAuthUserAuthenticatorImpl implements UserAuthenticator<OAuthUser> {

  private static final Logger LOG = LoggerFactory.getLogger(OAuthUserAuthenticatorImpl.class);

  private static final String CLIENT_ID = "client_id";
  private static final String CLIENT_SECRET = "client_secret";
  private static final String GRANT_TYPE = "grant_type";
  private static final String USERNAME = "username";
  private static final String PW = "password";
  private static final String SCOPE = "scope";

  private final OAuthService service;

  public OAuthUserAuthenticatorImpl() {

    final OAuthService serviceData = new OAuthService();
    serviceData.setGrantType(SimulationConfig.getServiceGrantType());
    serviceData.setClientCode(SimulationConfig.getServiceClientCode());
    serviceData.setClientSecret(SimulationConfig.getServiceClientSecret());
    serviceData.setClientId(SimulationConfig.getServiceClientId());

    this.service = new OAuthServiceAuthenticatorImpl(new OAuthServiceTokenResponseDeserializer()).authenticate(serviceData);
  }

  @Override
  public OAuthUser authenticate(User user) {
    try {
      var form = createFormObject(user);
      var response = executeRequest(form);

      handleNonOK(response);

      var responseData = new OAuthUserTokenResponseDeserializer().deserialize(response);

      return new OAuthUserImpl(service, user.getUsername(),
          user.getPassword(),
          responseData.getAccessToken(),
          responseData.getRefreshToken(),
          user.getScope(),
          SimulationConfig.getClientId(),
          user.getId(),
          user.getRegion());
    } catch (Exception e) {
      ExceptionUtils.rethrow(e, UserLoginException.class, "Login failed.");
    }

    return null;
  }

  private void handleNonOK(Response response) {
    if (response.getStatus() != Status.OK.getStatusCode()) {
      if (LOG.isInfoEnabled()) {
        LOG.info("Cannot login user, status={} message={}", response.getStatus(),
            response.readEntity(String.class));
      }
      throw new UserLoginException("Cannot authenticate the client: " + service.getClientId());
    }
  }

  private Response executeRequest(Form form) {
    var client = ClientBuilder.newClient();
    return client
        .target(SimulationConfig.getAuthServer())
        .request()
        .post(Entity.form(form));
  }

  private Form createFormObject(User user) {
    var form = new Form();

    if (SimulationConfig.getClientId() != null) {
      form.param(CLIENT_ID, SimulationConfig.getClientId());
    }

    if (SimulationConfig.getClientSecret() != null) {
      form.param(CLIENT_SECRET, SimulationConfig.getClientSecret());
    }

    if (SimulationConfig.getGrantType() != null) {
      form.param(GRANT_TYPE, SimulationConfig.getGrantType());
    }

    if (user.getScope() != null) {
      form.param(SCOPE, user.getScope());
    }

    if (user.getPassword() != null) {
      form.param(PW, user.getPassword());
    }

    form.param(USERNAME, user.getUsername());
    return form;
  }
}
