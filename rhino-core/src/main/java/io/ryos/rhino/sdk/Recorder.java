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

/**
 * Recorder is used in performance tests to record the result of execution. Recorded metrics will
 * be flushed into the storage registered.
 * <p>
 *
 * @author <a href="mailto:erhan@ryos.io">Erhan Bagdemir</a>
 */
public interface Recorder {

  /**
   * Call record(String, int) to record the temporal metrics.
   * <p>
   *
   * @param stepName The name of the step.
   * @param status HTTP status of the load execution.
   */
  void record(final String stepName, final int status);
}
