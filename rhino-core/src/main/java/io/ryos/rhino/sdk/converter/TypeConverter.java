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

package io.ryos.rhino.sdk.converter;

/**
 * Type converters are used to convert an object of source type to the destination type.
 *
 * @param <T> Target object type.
 * @author Erhan Bagdemir
 */
public interface TypeConverter<T> {

  /**
   * Returns a new {@link IntTypeConverter} instance.
   *
   * @return {@link IntTypeConverter} instance
   */
  static IntTypeConverter asInt() {
    return new IntTypeConverter();
  }

  /**
   * Returns a new {@link StringTypeConverter} instance.
   *
   * @return {@link StringTypeConverter} instance
   */
  static StringTypeConverter asStr() {
    return new StringTypeConverter();
  }

  /**
   * Returns a new {@link ListTypeConverter} instance.
   *
   * @param typeConverter Nested type converter to convert list items.
   * @return {@link ListTypeConverter} instance
   */
  static <E> ListTypeConverter<E> asList(TypeConverter<E> typeConverter) {
    return new ListTypeConverter<>(typeConverter);
  }

  /**
   * Returns a new {@link ListTypeConverter} instance.
   *
   * @return {@link ListTypeConverter} instance
   */
  static ListTypeConverter<String> asList() {
    return new ListTypeConverter<>(new StringTypeConverter());
  }

  /**
   * Convert {@link String} value to the target type.
   *
   * @param input Input string.
   * @return The value of input in target type.
   */
  T convert(String input);
}
