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

import io.ryos.rhino.sdk.simulations.NestedDslSimulation;
import org.junit.Test;

public class NestedDslSimulationTest {

  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Test
  public void testNested() {
    Simulation.getInstance(PROPERTIES_FILE, NestedDslSimulation.class).start();
  }
}
