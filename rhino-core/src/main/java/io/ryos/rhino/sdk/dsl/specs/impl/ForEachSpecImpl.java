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

package io.ryos.rhino.sdk.dsl.specs.impl;

import io.ryos.rhino.sdk.dsl.specs.ForEachSpec;
import io.ryos.rhino.sdk.dsl.specs.builder.ForEachBuilder;

/**
 * For-each loop representation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public class ForEachSpecImpl<E, R extends Iterable<E>> extends AbstractSpec implements ForEachSpec {

  /**
   * Builder implementation for {@link ForEachSpec}.
   * <p>
   */
  private final ForEachBuilder<E, R> forEachBuilder;

  /**
   * Constructs a new {@link ForEachSpec} instance.
   * <p>
   *
   * @param forEachBuilder Builder implementation for {@link ForEachSpec}.
   */
  public ForEachSpecImpl(ForEachBuilder<E, R> forEachBuilder) {
    super("N/A");

    this.forEachBuilder = forEachBuilder;
  }

  /**
   * Getter for {@link ForEachBuilder}.
   * <p>
   *
   * @return {@link ForEachBuilder} instance.
   */
  public ForEachBuilder<E, R> getForEachBuilder() {
    return forEachBuilder;
  }
}
