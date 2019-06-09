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

package io.ryos.rhino.sdk.specs;

import io.ryos.rhino.sdk.data.UserSession;
import java.util.Optional;
import java.util.function.Function;

/**
 * Common specification type implementation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public abstract class AbstractSpec implements Spec {

  private Function<UserSession, Spec> andThenFunction;

  @Override
  public Spec andThen(Function<UserSession, Spec> andThenFunction) {
    this.andThenFunction = (session) -> andThenFunction.apply(session).withName(this.getName());
    return this;
  }

  /**
   * Returns an {@link Optional} of function which returns a {@link Spec} for a {@link
   * UserSession}.
   * <p>
   *
   * @return An an {@link Optional} of binding function.
   */
  public Optional<Function<UserSession, Spec>> getAndThen() {
    return Optional.ofNullable(andThenFunction);
  }
}
