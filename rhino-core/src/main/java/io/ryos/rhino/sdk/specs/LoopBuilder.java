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

import java.util.function.Function;

/**
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public class LoopBuilder<E, R extends Iterable<E>> {

  private String key;
  private String saveTo;
  private Function<E, Spec> loopFunction;

  public LoopBuilder<E, R> in(String key) {
    this.key = key;
    return this;
  }

  public LoopBuilder<E, R> apply(Function<E, Spec> loopFunction) {
    this.loopFunction = loopFunction;
    return this;
  }

  public LoopBuilder<E, R> saveTo(String saveTo) {
    this.saveTo = saveTo;
    return this;
  }

  public String getKey() {
    return key;
  }

  public String getSaveTo() {
    return saveTo;
  }

  public Function<E, Spec> getLoopFunction() {
    return loopFunction;
  }
}
