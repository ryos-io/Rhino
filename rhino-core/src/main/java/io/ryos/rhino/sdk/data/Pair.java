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

/**
 * The type holding a pair of objects.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class Pair<T, E> {

  /**
   * The first item which the pair holds.
   * <p>
   */
  private final T first;

  /**
   * The second item which the pair holds.
   * <p>
   */
  private final E second;

  /**
   * Constructs a {@link Pair} instance.
   * <p>
   *
   * @param first The first value.
   * @param second The second value.
   */
  public Pair(T first, E second) {

    this.first = first;
    this.second = second;
  }

  /**
   * Getter for the first item.
   * <p>
   *
   * @return The first item.
   */
  public T getFirst() {
    return first;
  }

  /**
   * Getter for the second item.
   * <p>
   *
   * @return The second item.
   */
  public E getSecond() {
    return second;
  }
}
