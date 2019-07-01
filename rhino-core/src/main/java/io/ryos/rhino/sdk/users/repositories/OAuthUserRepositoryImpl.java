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

package io.ryos.rhino.sdk.users.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.data.UserSessionImpl;
import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import io.ryos.rhino.sdk.exceptions.UserLoginException;
import io.ryos.rhino.sdk.users.OAuthEntity;
import io.ryos.rhino.sdk.users.data.OAuthUserImpl;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.provider.UserProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response.Status;

public class OAuthUserRepositoryImpl implements UserRepository<UserSession> {

  private static final String CLIENT_ID = "client_id";
  private static final String CLIENT_SECRET = "client_secret";
  private static final String GRANT_TYPE = "grant_type";
  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  private static final String SCOPE = "scope";

  private final List<UserSession> authUsers;
  private final List<User> users;
  private final ExecutorService executorService;
  private final AtomicInteger cursor = new AtomicInteger(-1);
  private final long loginDelay;
  private final ObjectMapper objectMapper = new ObjectMapper();

  OAuthUserRepositoryImpl(final UserProvider userProvider, long loginDelay) {
    Objects.requireNonNull(userProvider);
    this.users = userProvider.getUsers();
    this.authUsers = new ArrayList<>(users.size());
    this.loginDelay = loginDelay;
    this.executorService = Executors.newFixedThreadPool(1);
  }

  OAuthUserRepositoryImpl authenticateAll() {
    System.out.println(String.format("! Found %d users. Authenticating with delay: %d ms ...", users.size(), loginDelay));
    users.forEach(u -> executorService.submit(() -> {
      delay();
      authenticate(u).ifPresent(a -> authUsers.add(new UserSessionImpl(a)));
    }));

    return this;
  }

  private void delay() {
    try {
      Thread.sleep(loginDelay);
    } catch (InterruptedException e) {
      // interrupted
    }
  }

  private Optional<User> authenticate(User user) {

    try {
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
        form.param(PASSWORD, user.getPassword());
      }

      form.param(USERNAME, user.getUsername());

      var client = ClientBuilder.newClient();
      var response = client
          .target(SimulationConfig.getAuthServer())
          .request()
          .post(Entity.form(form));

      if (response.getStatus() != Status.OK.getStatusCode()) {
        System.out.println("Cannot login user, status=" + response.getStatus() + ", message=" + response.readEntity(String.class));
        return Optional.empty();
      }

      var s = response.readEntity(String.class);

      var entity = mapToEntity(s);

      return Optional.of(new OAuthUserImpl(user.getUsername(),
          user.getPassword(),
          entity.getAccessToken(),
          entity.getRefreshToken(),
          user.getScope(),
          SimulationConfig.getClientId(),
          user.getId(),
          user.getRegion()));
    } catch (Exception e) {
      ExceptionUtils.rethrow(e, UserLoginException.class, "Login failed.");
    }

    return Optional.empty();
  }

  private OAuthEntity mapToEntity(final String s) {
    final OAuthEntity o;
    try {
      o = objectMapper.readValue(s, OAuthEntity.class);
    } catch (Throwable t) {
      throw new RuntimeException("Cannot map authorization server response to entity type: " + OAuthEntity.class.getName(), t);
    }
    return o;
  }

  public UserSession take() {
    cursor.getAndUpdate(p -> (p + 1) % authUsers.size());
    return authUsers.get(cursor.get());
  }

  public List<UserSession> getUserSessions() {
    return authUsers;
  }
}
