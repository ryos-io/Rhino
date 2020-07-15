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

package io.ryos.rhino.sdk.dsl.data.builder;

import io.ryos.rhino.sdk.dsl.DslBuilder;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.Validate;

/**
 * Helper builder used with map() DSL. Use {@link MapperBuilder} instances to complete the
 * {@link DslBuilder#map(MapperBuilder)} DSL.
 *
 * @author Erhan Bagdemir
 * @see DslBuilder#map(MapperBuilder)
 */
public class MapperBuilder<R, T> {

  private String key;
  private Function<R, T> mappingFunction;
  private String saveTo;

  private MapperBuilder(String key) {
    this.key = key;
    this.saveTo = key; // Default
  }

  /**
   * Use the method to access the session object with the key, sessionKey.
   *
   * @param sessionKey The session key of the object being accessed.
   * @param <R>        The type of the session object.
   * @param <T>        Target type to which the session object will be mapped.
   * @return {@link MapperBuilder} instance.
   */
  public static <R, T> MapperBuilder<R, T> from(String sessionKey) {
    Validate.notEmpty(sessionKey, "Session key must not be empty.");
    return new MapperBuilder<>(Objects.requireNonNull(sessionKey));
  }

  /**
   * Use to provide a {@link Function} instance which will be applied to the input object and
   * returns the target object.
   *
   * @param mappingFunction The function which is used to map the input object to the output.
   * @return {@link MapperBuilder} instance.
   */
  public MapperBuilder<R, T> doMap(Function<R, T> mappingFunction) {
    Validate.notNull(mappingFunction, "Mapping function must not be null.");
    this.mappingFunction = mappingFunction;
    return this;
  }

  /**
   * After map function is applied, use this method to store the output object in the session.
   *
   * @param sessionKey Session key for the output instance.
   * @return {@link MapperBuilder} instance.
   */
  public MapperBuilder<R, T> saveTo(String sessionKey) {
    Validate.notEmpty(sessionKey, "Session key must not be empty.");
    this.saveTo = sessionKey;
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
