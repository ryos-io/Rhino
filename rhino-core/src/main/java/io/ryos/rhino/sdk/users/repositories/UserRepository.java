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
import java.util.List;

/**
 * User repository is a storage for test user.
 * <p>
 *
 * @param <T> UserSession.
 * @author Erhan Bagdemir
 */
public interface UserRepository<T extends UserSession> {

  List<T> leaseUsers(int numberOfUsers, String region);
  List<T> leaseUsers(int numberOfUsers);

}
