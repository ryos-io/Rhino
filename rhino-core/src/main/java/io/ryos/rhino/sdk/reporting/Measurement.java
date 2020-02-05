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

import java.util.List;

/**
 * Measurement is used in performance tests to measure the result of execution. Recorded metrics will
 * be flushed into the storage registered.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public interface Measurement {

  /**
   * Stops the measurement by committing the measurement to the dispatcher.
   *
   * @param message Failure description.
   */
  void fail(String message);

  /**
   * Starts the measurement.
   */
  void start();

  /**
   * Finishes the measurement by committing it to the dispatcher.
   */
  void finish();

  /**
   * Call measure(String, int) to measure the temporal metrics.
   * <p>
   *
   * @param stepName The name of the step.
   * @param status   HTTP status of the load execution.
   */
  void measure(String stepName, String status);

  public void measure(String status);

  /**
   * The {@link LogEvent} will be recorded and stored internally. The
   * {@link io.ryos.rhino.sdk.runners.EventDispatcher} will then dispatch the persisted events to
   * the instances which process these events.
   * <p>
   *
   * @param event log event.
   */
  void record(LogEvent event);

  /**
   * Purge the backing data structure by deleting all the events stored.
   * <p>
   */
  void purge();

  List<LogEvent> getEvents();
}
