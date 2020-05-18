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

package io.ryos.rhino.sdk.runners;

import io.ryos.rhino.sdk.SimulationMetadata;

/**
 * Simulation runner. The implementations of this type run the simulation instances.
 * <p>
 *
 * @author Erhan Bagdemir
 * @version 1.0.0
 * @see SimulationMetadata
 */
public interface SimulationRunner {

  /**
   * Starts the simulation instance.
   * <p>
   */
  void start();

  /**
   * Starts the simulation instance.
   * <p>
   */
  void verify();

  /**
   * Stop the simulation instance, by shutting down all components.
   * <p>
   */
  void stop();
}
