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

import java.util.Objects;
import java.util.function.Function;

/**
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class MapperBuilder<R, T> {

  private String key;
  private Function<R, T> mappingFunction;
  private String saveTo;

  private MapperBuilder(String key) {
    this.key = key;
    this.saveTo = key; // Default
  }

  public static <R, T> MapperBuilder<R, T> from(String key) {
    return new MapperBuilder<>(Objects.requireNonNull(key));
  }

  public MapperBuilder<R, T> doMap(Function<R, T> mappingFunction) {
    this.mappingFunction = mappingFunction;
    return this;
  }

  public MapperBuilder<R, T> saveTo(String saveTo) {
    this.saveTo = saveTo;
    return this;
  }

  public String getKey() {
    return key;
  }

  public Function<R, T> getMappingFunction() {
    return mappingFunction;
  }

  public String getSaveTo() {
    return saveTo;
  }
}