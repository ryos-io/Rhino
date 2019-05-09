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
 * Default log formatter used in step log results.
 *
 * @author <a href="mailto:erhan@ryos.io">Erhan Bagdemir</a>
 */
public class DefaultLogFormatter implements LogFormatter {

  @Override
  public String format(final LogEvent event) {
    return String.format("\"%s\"\t\"%s\"\t%s\t%s\t%s\t%s\n",
        event.scenario,
        event.start,
        event.elapsed,
        event.username,
        event.start
    );
  }
}
