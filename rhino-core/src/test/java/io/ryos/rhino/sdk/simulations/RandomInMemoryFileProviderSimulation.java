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

package io.ryos.rhino.sdk.simulations;


import static io.ryos.rhino.sdk.dsl.LoadDsl.dsl;
import static io.ryos.rhino.sdk.dsl.MaterializableDslItem.some;

import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.providers.RandomInMemoryFileProvider;

@Simulation(name = "Random File Test")
public class RandomInMemoryFileProviderSimulation {

  @Provider(clazz = RandomInMemoryFileProvider.class)
  private RandomInMemoryFileProvider fileProvider;

  @Dsl(name = "Random in memory file")
  public LoadDsl testRandomFiles() {
    return dsl().run(some("test").as(s -> {
          return "OK";
        }));
  }
}
