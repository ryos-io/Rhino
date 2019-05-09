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

package com.adobe.rhino.sdk.users;

import com.adobe.rhino.sdk.SimulationConfig;
import com.adobe.rhino.sdk.data.UserSession;

/**
 * Factory class for IMS user repository which creates a new {@link UserRepository} provides
 * {@link User} instances authenticated.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 */
public class IMSUserRepositoryFactoryImpl implements UserRepositoryFactory<UserSession> {

  private final String pathToUsers;
  private final long loginDelay;

  public IMSUserRepositoryFactoryImpl(final long loginDelay) {
    final String userSource = SimulationConfig.getUserSource();
    this.pathToUsers = userSource.replace("classpath://", "");
    this.loginDelay = loginDelay;
  }

  @Override
  public UserRepository<UserSession> create() {
    return new IMSUserRepositoryImpl(new ClasspathUserProviderImpl(pathToUsers), loginDelay).authenticateAll();
  }
}
