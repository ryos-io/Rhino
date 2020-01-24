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
import io.ryos.rhino.sdk.annotations.RampUp;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.impl.SomeDslImpl;
import io.ryos.rhino.sdk.providers.UUIDProvider;

/**
 * Reactive test spec for arbitrary code execution with {@link SomeDslImpl}.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
@Simulation(name = "Reactive Sleep Test")
@RampUp(startRps = 1, targetRps = 10)
public class ReactiveSleepTestSimulation {

  @Provider(clazz = UUIDProvider.class)
  private UUIDProvider provider;

  @Dsl(name = "Sleep Test")
  public LoadDsl testSleep() {
    return dsl().run(some("Sleeping 1 sec.").exec(session -> {
      System.out.println(provider.take());
      return "OK";
    }));
  }
}
