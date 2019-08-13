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

import io.ryos.rhino.sdk.dsl.specs.Spec;
import java.util.function.Function;

/**
 * Loop builder is a builder providing the spec with looping information to be executed.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public class ForEachBuilder<E, R extends Iterable<E>> {

  private String key;
  private String saveTo;
  private Function<E, Spec> forEachFunction;

  public ForEachBuilder(final String key) {
    this.key = key;
  }

  public static <E, R extends Iterable<E>> ForEachBuilder<E, R> in(String key) {
    return new ForEachBuilder<>(key);
  }

  public ForEachBuilder<E, R> apply(Function<E, Spec> forEachFunction) {
    this.forEachFunction = forEachFunction;
    return this;
  }

  public ForEachBuilder<E, R> saveTo(String saveTo) {
    this.saveTo = saveTo;
    return this;
  }

  public String getKey() {
    return key;
  }

  public String getSaveTo() {
    return saveTo;
  }

  public Function<E, Spec> getForEachFunction() {
    return forEachFunction;
  }
}
