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
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

import io.ryos.rhino.sdk.simulations.NonUniqueSimpleBlockingSimulationOne;
import io.ryos.rhino.sdk.simulations.NonUniqueSimpleBlockingSimulationTwo;
import io.ryos.rhino.sdk.simulations.SimpleBlockingSimulation;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Test;

public class SimulationJobsScannerTest {

  private static final String PACKAGE_TO_SCAN = "io.ryos.rhino.sdk.simulations";
  private static final String SIMULATION = "Simple Blocking Simulation";
  private static final String NON_UNIQUE_SIM = "Simple Blocking Simulation Not Unique Test";

  @Test
  public void testSimulationScanWithScenarios() {

    SimulationJobsScannerImpl scanner = new SimulationJobsScannerImpl();
    List<SimulationMetadata> metadataList = scanner.scan(SIMULATION, PACKAGE_TO_SCAN);
    assertThat(metadataList, notNullValue());
    assertThat(metadataList.size(), equalTo(1));
    assertThat(metadataList.get(0).getSimulationClass(), equalTo(
        SimpleBlockingSimulation.class));
  }

  @Test
  public void testSimulationScanWithNonUniqueSimulations() {

    SimulationJobsScannerImpl scanner = new SimulationJobsScannerImpl();
    List<SimulationMetadata> metadataList = scanner.scan(
        NON_UNIQUE_SIM, PACKAGE_TO_SCAN);
    assertThat(metadataList, notNullValue());
    assertThat(metadataList.size(), equalTo(2));

    List<Class> clazzes = metadataList.stream()
        .map(SimulationMetadata::getSimulationClass)
        .collect(Collectors.toList());
    assertThat(clazzes, hasItems(NonUniqueSimpleBlockingSimulationOne.class,
        NonUniqueSimpleBlockingSimulationTwo.class));
  }

  @Test
  public void testSimulationScanForNonExisting() {

    SimulationJobsScannerImpl scanner = new SimulationJobsScannerImpl();
    List<SimulationMetadata> metadataList = scanner.scan(UUID.randomUUID().toString(), PACKAGE_TO_SCAN);
    assertThat(metadataList, notNullValue());
    assertThat(metadataList.size(), equalTo(0));
  }

  @Test
  public void testSimulationScanForNonExistingPackage() {

    SimulationJobsScannerImpl scanner = new SimulationJobsScannerImpl();
    List<SimulationMetadata> metadataList = scanner.scan(SIMULATION,  UUID.randomUUID().toString());
    assertThat(metadataList, notNullValue());
    assertThat(metadataList.size(), equalTo(0));
  }
}
