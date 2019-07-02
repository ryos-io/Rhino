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
import io.ryos.rhino.sdk.users.source.UserSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OAuthUserRepositoryImpl implements UserRepository<UserSession> {

  private final long loginDelay;
  private final OAuthAuthenticatorImpl authenticator;

  private final UserSource userSource;

  OAuthUserRepositoryImpl(final UserSource userSource, long loginDelay) {
    this.userSource = Objects.requireNonNull(userSource);
    this.authenticator = new OAuthAuthenticatorImpl();
    this.loginDelay = loginDelay;
  }

  public List<UserSession> leaseUsers(int numberOfUsers, String region) {
    var users = userSource.getUsers(numberOfUsers, region);
    var result = new ArrayList<UserSession>();

    users.forEach(u -> {
      delay();
      var userSession = new UserSessionImpl(authenticator.authenticate(u));
      result.add(userSession);
    });

    return result;
  }

  @Override
  public List<UserSession> leaseUsers(int numberOfUsers) {
    return leaseUsers(numberOfUsers, Regions.ALL);
  }


  private void delay() {
    try {
      Thread.sleep(loginDelay);
    } catch (InterruptedException e) {
      // interrupted
    }
  }
}
