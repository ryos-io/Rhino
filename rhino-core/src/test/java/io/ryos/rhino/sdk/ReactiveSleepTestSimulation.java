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

package io.ryos.rhino.sdk;

import static io.ryos.rhino.sdk.specs.Spec.some;

import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.RampUp;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.Throttle;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.feeders.UUIDProvider;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import java.time.Duration;
import java.util.Random;

/**
 * Reactive test spec for arbitrary code execution with {@link io.ryos.rhino.sdk.specs.SomeSpecImpl}.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
@Simulation(name = "Reactive Sleep Test")
@Runner(clazz = ReactiveHttpSimulationRunner.class)
@RampUp(startRps = 1, targetRps = 10)
public class ReactiveSleepTestSimulation {

  @Provider(factory = UUIDProvider.class)
  private UUIDProvider provider;

  @Dsl(name = "Sleep Test")
  public LoadDsl testSleep() {
    return Start
        .spec()
        .run(some("Sleeping 1 sec.").as((u, m) -> {
          m.measure("1. measurement", "OK");
          return u;
        }));
  }

  private void waitASec() {
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      // Intentionally left empty.
    }
  }
}
