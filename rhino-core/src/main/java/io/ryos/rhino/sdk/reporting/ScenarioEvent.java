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

public class ScenarioEvent extends LogEvent {

  private final String status;
  private final String step;

  public ScenarioEvent(String username, String userId, String scenario,
      long start,
      long end,
      long elapsed,
      String status,
      String step) {
    super(username, userId, scenario, start, end, elapsed);
    this.status = status;
    this.step = step;
  }

  public String getStatus() {
    return status;
  }

  public String getStep() {
    return step;
  }

  @Override
  public String toString() {
    return "ScenarioEvent{" +
        "status='" + status + '\'' +
        ", step='" + step + '\'' +
        ", username='" + getUsername() + '\'' +
        ", userId='" + getUserId() + '\'' +
        ", scenario='" + getScenario() + '\'' +
        ", start=" + getStart() +
        ", end=" + getEnd() +
        ", elapsed=" + getElapsed() +
        '}';
  }
}
