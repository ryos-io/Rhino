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

import static java.util.stream.Collectors.toList;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.data.UserImpl;
import io.ryos.rhino.sdk.users.source.UserSource;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * User repository, if no repository is provided. It samples users which are used in simulations.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class DefaultUserRepositoryFactory implements UserRepositoryFactory<UserSession> {

  private static final int START = 1;
  private static final int END = 10;

  @Override
  public UserRepository<UserSession> create() {

    var userName = "User-" + UUID.randomUUID();
    var defaultSource = new UserSource() {
      @Override
      public List<User> getUsers() {
        return IntStream.rangeClosed(START, END)
            .mapToObj(id -> new UserImpl(userName, null, userName, null))
            .collect(toList());
      }

      @Override
      public List<User> getUsers(int numberOfUsers, String region) {
        return getUsers();
      }
    };

    return new DefaultUserRepositoryImpl(defaultSource);
  }
}
