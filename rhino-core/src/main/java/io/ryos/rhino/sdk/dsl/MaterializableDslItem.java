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

package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.dsl.impl.HttpDslImpl;
import io.ryos.rhino.sdk.dsl.impl.SomeDslImpl;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;

/**
 * Load testing specification for reactive runner.
 *
 * @author Erhan Bagdemir
 * @see ReactiveHttpSimulationRunner
 * @since 1.1.0
 */
public interface MaterializableDslItem extends DslItem, MaterializableDsl {

  /**
   * Static factory method to create a new {@link HttpDsl} instance.
   *
   * @param name Measurement point name.
   * @return A new instance of {@link MaterializableDslItem}.
   */
  static HttpConfigDsl http(String name) {
    return new HttpDslImpl(name);
  }

  static SomeDsl some(String name) {
    return new SomeDslImpl(name);
  }
}
