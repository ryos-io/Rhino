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

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.data.UserSessionImpl;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.source.UserSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class OAuthUserRepositoryImpl implements UserRepository<UserSession> {

  private final List<UserSession> authUsers;
  private final List<User> users;
  private final ExecutorService executorService;
  private final AtomicInteger cursor = new AtomicInteger(-1);
  private final long loginDelay;
  private final OAuthAuthenticatorImpl authenticator;

  OAuthUserRepositoryImpl(final UserSource userSource, long loginDelay) {
    Objects.requireNonNull(userSource);

    this.authenticator = new OAuthAuthenticatorImpl();
    this.users = userSource.getUsers();
    this.authUsers = new ArrayList<>(users.size());
    this.loginDelay = loginDelay;
    this.executorService = Executors.newFixedThreadPool(1);
  }

  OAuthUserRepositoryImpl authenticateAll() {
    System.out.println(String
        .format("! Found %d users. Authenticating with delay: %d ms ...", users.size(),
            loginDelay));
    users.forEach(u -> executorService.submit(() -> {
      delay();
      var userSession = new UserSessionImpl(authenticator.authenticate(u));
      authUsers.add(userSession);
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

  public UserSession take() {
    cursor.getAndUpdate(p -> (p + 1) % authUsers.size());
    return authUsers.get(cursor.get());
  }

  public List<UserSession> getUserSessions() {
    return authUsers;
  }
}
