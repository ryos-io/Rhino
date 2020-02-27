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

package io.ryos.rhino.sdk.data;

import io.ryos.rhino.sdk.reporting.Measurement;
import java.util.Optional;


/**
 * ContextImpl type for storing values throughout a testing session. Each session - and thread
 * respectfully, must have a single context instance bound.
 *
 * @author Erhan Bagdemir
 * @since 1.0.0
 */
public interface Context {

  /**
   * Puts a new key - value pair to the context.
   *
   * @param key Key value.
   * @param value Value to store.
   */
  <T extends Context> T add(String key, Object value);

  /**
   * Reclaims the object from the context.
   *
   * @param key The key value.
   * @param <T> The value stored in the context of type {@code T}
   * @return An {@link Optional} instance of {@code T}.
   */
  <T> Optional<T> get(String key);

  /**
   * Empties the context.
   */
  void empty();

  /**
   * Checks whether the context is empty.
   * <p>
   * 
   * @return true if the context is empty.
   */
  boolean isEmpty();

  void register(Measurement measurement);

  void notify(long time);

  void commit(String status);

  void remove(Measurement measurement);
}
