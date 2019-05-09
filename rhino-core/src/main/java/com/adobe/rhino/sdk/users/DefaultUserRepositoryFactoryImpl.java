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

import static java.util.stream.Collectors.toList;

import com.adobe.rhino.sdk.data.UserSession;
import com.adobe.rhino.sdk.data.UserSessionImpl;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * User repository, if nothing else provided.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 */
public class DefaultUserRepositoryFactoryImpl implements UserRepositoryFactory<UserSession> {

  private static final int START = 1;
  private static final int END = 10;

  @Override
  public UserRepository<UserSession> create() {

    return new DefaultUserRepositoryImpl(() -> IntStream
        .rangeClosed(START, END)
        .mapToObj(id -> new UserImpl("User-" + UUID.randomUUID(), null,
            id, null)).collect(toList()));
  }
}
