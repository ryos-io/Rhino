/*
  Copyright 2018 Adobe.

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

package com.adobe.rhino.sdk.io;

import com.adobe.rhino.sdk.reporting.LogEvent;
import java.io.Closeable;

/**
 * Simulation result writer logs the benchmark metrics into a flat file, that is the simulation
 * file.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 */
public interface ResultWriter<T extends LogEvent> extends Closeable {

  /**
   * Adds a new log event into the simulation logging source.
   *
   * @param report Log event.
   */
  void report(T report);

  /**
   * Adds a new log event as string into the simulation logging source.
   *
   * @param report Log event.
   */
  void report(String report);
}
