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

package io.ryos.rhino.sdk.reporting;

/**
 * Measurement is used in performance tests to measure the result of execution. Recorded metrics will
 * be flushed into the storage registered.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public interface Measurement {

  /**
   * Call measure(String, int) to measure the temporal metrics.
   * <p>
   *
   * @param stepName The name of the step.
   * @param status HTTP status of the load execution.
   */
  void measure(final String stepName, final String status);
}
