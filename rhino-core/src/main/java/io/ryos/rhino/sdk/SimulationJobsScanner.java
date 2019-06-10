/*
  Copyright 2018 Ryos.io.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package io.ryos.rhino.sdk;

import java.util.List;

/**
 * Scanner, used to search for annotated benchmark entities within the package provided. The with
 * {@link io.ryos.rhino.sdk.annotations.Simulation} annotated entities will be packaged along
 * with the SDK into a JAR file, so the scanner searches for entities in the JAR artifact.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see io.ryos.rhino.sdk.annotations.Simulation
 * @see SimulationMetadata
 * @since 1.0
 */
public interface SimulationJobsScanner {

    /**
     * Scanner method which takes a list of paths to be scanned for benchmark entities and
     * returns a list of {@link SimulationMetadata} instances.
     *
     * @param inPackages The path to scan for entities.
     * @param forSimulation Simulation name.
     * @return A list of benchmark job instances.
     */
    List<SimulationMetadata> scan(String forSimulation, String... inPackages);

    /**
     * Factory method to create new {@link SimulationJobsScanner} instances.
     *
     * @return An instance of the scanner.
     */
    static SimulationJobsScanner create() {
        return new SimulationJobsScannerImpl();
    }
}
