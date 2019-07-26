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

package io.ryos.rhino.sdk.annotations;

import io.ryos.rhino.sdk.reporting.DefaultSimulationLogFormatter;
import io.ryos.rhino.sdk.reporting.SimulationLogFormatter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Class annotation to provide metadata about logging strategy. The benchmark metrics will be
 * written into the log file where this annotation points.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see SimulationLogFormatter
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Logging {

  /**
   * The path where benchmark metrics should be written.
   * <p>
   *
   * @return The path where to log.
   */
  String file();

  /**
   * Log formatter.
   * <p>
   *
   * @return Log formatter instance.
   */
  Class<? extends SimulationLogFormatter> formatter() default DefaultSimulationLogFormatter.class;
}
