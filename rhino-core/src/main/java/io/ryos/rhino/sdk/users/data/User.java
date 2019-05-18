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

package io.ryos.rhino.sdk.users.data;

/**
 * Representation of a User, that is used in requests against the backend. Every request is
 * associated with an user.
 *
 * @author <a href="mailto:erhan@ryos.io">Erhan Bagdemir</a>
 * @since 1.0.0
 */
public interface User {

  /**
   * User name.
   *
   * @return User name.
   */
  String getUsername();

  /**
   * Password, if authentication is required.
   *
   * @return Password.
   */
  String getPassword();

  /**
   * Scope of the user.
   *
   * @return Authorization scope.
   */
  String getScope();

  /**
   * A unique id to distinguish the user from others.
   *
   * @return The id of the user.
   */
  int getId();
}
