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

package io.ryos.rhino.sdk.providers;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.converter.TypeConverter;

public abstract class AbstractProvider<T> implements Provider<T> {

  public abstract String name();

  protected <E> E getEnvConfig(String property, TypeConverter<E> converter) {
    return converter.convert(SimulationConfig.getEnvConfig(name(), property));
  }

  protected <E> E getConfig(String property, TypeConverter<E> converter) {
    return converter.convert(SimulationConfig.getConfig(name(), property));
  }
}
