/*
 * Copyright 2018 Ryos.io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ryos.rhino.sdk.users.repositories;

import io.ryos.rhino.sdk.data.UserSession;

/**
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface RegionalUserProvider<T> {

  /**
   * Method to determine if the repository contains the number of users, provided by parameter.
   * <p>
   *
   * @param numberOfUsers Number of users, to be queried.
   * @return true, if the repository contains sufficient number of users, otherwise false.
   */
  boolean has(int numberOfUsers);

  /**
   * Takes a single user from the repository.
   * <p>
   *
   * @return A {@link UserSession} instance, at the top of the list.
   */
  T take();
}
