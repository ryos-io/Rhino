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

public interface TypeConverter<T> {

  static IntTypeConverter asInt() {
    return new IntTypeConverter();
  }

  static StringTypeConverter asStr() {
    return new StringTypeConverter();
  }

  static <E> ListTypeConverter<E> asList(TypeConverter<E> typeConverter) {
    return new ListTypeConverter<E>(typeConverter);
  }

  static ListTypeConverter<String> asList() {
    return new ListTypeConverter<>(new StringTypeConverter());
  }

  T convert(String input);
}
