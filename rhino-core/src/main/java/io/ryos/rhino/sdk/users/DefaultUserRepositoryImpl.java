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

package io.ryos.rhino.sdk.users;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.data.UserSessionImpl;
import io.ryos.rhino.sdk.users.data.User;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class DefaultUserRepositoryImpl implements UserRepository<UserSession> {

  private Queue<User> users;

  public DefaultUserRepositoryImpl(UserProvider userProvider) {
    Objects.requireNonNull(userProvider);
    this.users = new LinkedBlockingQueue<>(userProvider.getUsers());
  }

  @Override
  public UserSession take() {
    User user = users.peek();
    users.add(user);
    return new UserSessionImpl(user);
  }

  @Override
  public boolean has(int numberOfUsers) {
    return users.size() >= numberOfUsers;
  }

  @Override
  public List<UserSession> getUserSessions() {
    return users.stream().map(UserSessionImpl::new).collect(Collectors.toList());
  }
}
