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

package io.ryos.rhino.sdk.dsl.specs;

/**
 * Retriable spec is the DSL spec which is to be retried if predicate turns true.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface MeasurableSpec extends DSLSpec {

  /**
   * Whether the measurement is enabled.
   * <p>
   *
   * @return True if measurement is enabled.
   */
  boolean isMeasurementEnabled();

  /**
   * Whether the measurement is cumulative.
   * <p>
   *
   * @return True if cumulative measurement is enabled.
   */
  boolean isCumulative();

  /**
   * The name of the spec. It is the step name in scenario counterpart.
   * <p>
   *
   * @return The name of the spec.
   */
  String getMeasurementPoint();

  /**
   * Disables the measurement recording.
   * <p>
   *
   * @return {@link HttpConfigSpec} instance.
   */
  DSLSpec noMeasurement();

  /**
   * Cumulative measurement.
   * <p>
   *
   * @return {@link HttpConfigSpec} instance.
   */
  DSLSpec cumulative();
}
