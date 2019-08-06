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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

import io.ryos.rhino.sdk.simulations.SimpleBlockingSimulation;
import java.util.List;
import org.junit.Test;

public class SimulationJobsScannerTest {

  private static final String PACKAGE_TO_SCAN = "io.ryos.rhino.sdk.simulations";
  private static final String SIMULATION = "Simple Blocking Simulation";

  @Test
  public void testSimulationScanWithScenarios() {

    SimulationJobsScannerImpl scanner = new SimulationJobsScannerImpl();
    List<SimulationMetadata> metadataList = scanner.scan(SIMULATION, PACKAGE_TO_SCAN);
    assertThat(metadataList, notNullValue());
    assertThat(metadataList.size(), equalTo(1));
    assertThat(metadataList.get(0).getSimulationClass(), equalTo(
        SimpleBlockingSimulation.class));
  }
}
