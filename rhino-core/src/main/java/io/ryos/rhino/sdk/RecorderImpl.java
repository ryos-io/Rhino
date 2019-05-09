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

import io.ryos.rhino.sdk.reporting.LogEvent;
import io.ryos.rhino.sdk.reporting.SimulationEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Recorder implementation which measures elapsed time from beginning the execution of the
 * scenario.
 *
 * @author <a href="mailto:erhan@ryos.io">Erhan Bagdemir</a>
 */
public class RecorderImpl implements Recorder {

  private final List<LogEvent> events = new ArrayList<>();
  private final String scenarioName;
  private final int userId;

  public RecorderImpl(final String scenarioName, final int userId) {
    this.scenarioName = scenarioName;
    this.userId = userId;
  }

  @Override
  public void record(final String stepName, final int status) {
    final long end = System.currentTimeMillis();
    final LogEvent lastEvent = events.get(events.size() - 1);
    final long start = lastEvent.end;

    final SimulationEvent simLog = new SimulationEvent();
    simLog.elapsed = end - start;
    simLog.start = start;
    simLog.end = end;
    simLog.step = stepName;
    simLog.scenario = scenarioName;
    simLog.userId = userId;
    simLog.status = Integer.toString(status);

    events.add(simLog);
  }

  void record(final LogEvent event) {
    events.add(event);
  }

  public List<LogEvent> getEvents() {
    return events;
  }
}
