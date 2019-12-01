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

package io.ryos.rhino.sdk.providers;

/**
 * Provider is object source to create new instances of type, T, to inject them into injection
 * points annotated with {@link Provider}.
 * <p>
 *
 * @param <T> Type of object being fed.
 * @author Erhan Bagdemir
 * @version 1.0.0
 */
public interface Provider<T> {

  /**
   * Instance to be fed into injection point.
   * <p>
   *
   * @return Instance to be injected.
   */
  T take();

  String name();
}
