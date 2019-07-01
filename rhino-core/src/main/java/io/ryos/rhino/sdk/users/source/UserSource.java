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

package io.ryos.rhino.sdk.users.source;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.users.data.User;
import java.util.List;
import java.util.Optional;

/**
 * User source is the source for the users which are used in performance tests.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public interface UserSource {

  enum SourceType {FILE, VAULT}

  /**
   * Returns a list of {@link User}s.
   * <p>
   *
   * @return List of users.
   */
  List<User> getUsers();

  List<User> getUsers(int numberOfUsers, String region);

  static UserSource newSource() {

    UserSource userProvider = null;
    var userSource = SimulationConfig.getUserSource();
    if (userSource.equals(SourceType.VAULT)) {
      userProvider = new VaultUserSourceImpl();
    }
    if (userSource.equals(SourceType.FILE)) {
      var filePath = Optional
          .ofNullable(SimulationConfig.getUserFileSource())
          .orElseThrow(() -> new RuntimeException("<env>.users.file property is missing."));

      userProvider = new FileBasedUserSourceImpl(filePath);
    }

    return userProvider;
  }
}
