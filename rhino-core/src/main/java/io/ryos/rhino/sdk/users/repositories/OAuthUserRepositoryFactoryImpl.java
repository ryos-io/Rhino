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
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.source.UserSource;

/**
 * Factory class for user repository with OAuth support which creates a new {@link UserRepository}
 * provides {@link User} instances authenticated.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class OAuthUserRepositoryFactoryImpl implements UserRepositoryFactory<UserSession> {

  /**
   * Planned delay between two login attempts. Some authorization servers throttles requests if the
   * number of requests exceeds some limit. The configuration is to cope with this limitation.
   */
  private final long loginDelay;

  /**
   * Creates a new {@link OAuthUserRepositoryFactoryImpl} instance.
   * <p>
   *
   * @param loginDelay Delay between two login attempts.
   */
  public OAuthUserRepositoryFactoryImpl(final long loginDelay) {
    this.loginDelay = loginDelay;
  }

  /**
   * Creates a new repository {@link OAuthUserRepositoryFactoryImpl} instance.
   * <p>
   *
   * @return New repository instance.
   */
  public UserRepository<UserSession> create() {
    return new OAuthUserRepositoryImpl(UserSource.newSource(), loginDelay);
  }
}
