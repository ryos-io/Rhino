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
 * Gatling log formatter is used to format the step log entries so that the Gatling tooling
 * can process them to create load testing reports. Gatling reports can be created by running
 * gatling.sh in the command-line:
 *
 * <pre>
 *   $ gatling.sh -ro &lt;path-to-dir-containing-simulations&gt;
 * </pre>
 *
 * Gatling step log might change in future, so the current format is associated with the
 * version defined with GATLING_VERSION.
 *
 * @author <a href="mailto:erhan@ryos.io">Erhan Bagdemir</a>
 * @since 1.0
 */
public class GatlingLogFormatter implements LogFormatter {

  public static final String GATLING_VERSION = "3.0.0-RC4";
  public static final String GATLING_HEADLINE_TEMPLATE = "RUN\t%s\t%s\t%s\trhino\t%s\n";

  /**
   * Gatling formatter, writes the log event in the Gatling format so that Gatling can generate
   * reports:
   *
   * @param event Log event.
   * @return Formatted string containing the step entry.
   */
  @Override
  public String format(final LogEvent event) {
    if (event instanceof ScenarioEvent) {
      return convert((ScenarioEvent) event);
    }

    if (event instanceof UserEvent) {
      return convert((UserEvent) event);
    }

    return "N/A";
  }

  private String convert(ScenarioEvent event) {
    return String.format("REQUEST\t%s\t\t%s\t%s\t%s\t%s\t \n",
        event.userId,
        event.step,
        event.start,
        event.end,
        event.status.equals("200") ? "OK" : "KO"
    );
  }

  private String convert(UserEvent event) {
    return String.format("USER\t%s\t%d\t%s\t%d\t%d\n",
        event.scenario,
        event.id,
        event.eventType,
        event.start,
        event.end
    );
  }
}
