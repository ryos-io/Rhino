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

/**
 * Simulation controller to start and stop simulations. Simulations will be run depending on the
 * method being called as load, performance or verification tests.
 *
 * @author Erhan Bagdemir
 */
public interface SimulationRunner {

  /**
   * Starts a simulation instance for load testing. The test will be run till the time is over, that
   * is defined in the {@link io.ryos.rhino.sdk.annotations.Simulation} annotation, or {@link
   * io.ryos.rhino.sdk.dsl.EnsureDsl} DSL item fails.
   */
  void start();

  /**
   * Starts a simulation instance for verification testing in which the test will be run once and
   * depending on {@link io.ryos.rhino.sdk.dsl.VerifiableDslItem} DSL, the test will fail or pass.
   */
  void verify();

  /**
   * Starts a simulation instance for the number of times given as numberOfRepeats parameter. This
   * method is handy if you run performance tests and take samples for fix sized executions.
   *
   * @param numberOfRepeats Number of cycles the simulation needs to run.
   */
  void times(int numberOfRepeats);

  /**
   * Stop the simulation instance immediately by shutting down all components.
   */
  void stop();
}
