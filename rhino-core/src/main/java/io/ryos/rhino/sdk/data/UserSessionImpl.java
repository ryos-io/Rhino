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

package io.ryos.rhino.sdk.data;

import io.ryos.rhino.sdk.dsl.LoadToken;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.oauth.OAuthUser;
import java.util.List;
import java.util.Optional;

/**
 * User session is a stash to store objects and share them among scenarios or DSLs in every load
 * generation cycle. Once load generation cycle completes,
 * will be created before the simulation starts, and it will existing during the simulation
 * execution.
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class UserSessionImpl extends ContextImpl implements UserSession {

  private final SimulationSession simulationSession;
  private final User user;
  private final List<LoadToken> tokens;

  public UserSessionImpl(
      final User user,
      final SimulationSession session,
      final List<LoadToken> tokens) {
    this.user = user;
    this.simulationSession = session;
    this.tokens = tokens;
  }

  public UserSessionImpl(final User user) {
    this(user, null, null);
  }

  @Override
  public User getUser() {
    return user;
  }

  @Override
  public Optional<String> getUserToken() {
    if (user instanceof OAuthUser) {
      return Optional.ofNullable(((OAuthUser) user).getAccessToken());
    }
    return Optional.empty();
  }

  @Override
  public Optional<String> getRefreshToken() {
    if (user instanceof OAuthUser) {
      return Optional.ofNullable(((OAuthUser) user).getRefreshToken());
    }
    return Optional.empty();
  }

  public SimulationSession getSimulationSession() {
    return simulationSession;
  }

  public SimulationSession getSimulationSessionFor(User user) {
    return tokens
        .stream()
        .filter(t -> t.getUser().getUsername().equals(user.getUsername()))
        .findFirst()
        .map(LoadToken::getSimulationSession)
        .orElse(new SimulationSession(user));
  }
}
