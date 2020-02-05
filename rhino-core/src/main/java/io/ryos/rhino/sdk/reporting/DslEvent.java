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

public class DslEvent extends LogEvent {

  private final String status;
  private final String measurementPoint;

  public DslEvent(String username, String userId, String parentMeasurement,
      long start,
      long end,
      long elapsed,
      String status,
      String measurement) {
    super(username, userId, parentMeasurement, start, end, elapsed);

    this.status = status;
    this.measurementPoint = measurement;
  }

  public String getStatus() {
    return status;
  }

  public String getMeasurementPoint() {
    return measurementPoint;
  }

  @Override
  public String toString() {
    return "DslEvent{" +
        "status='" + status + '\'' +
        ", measurementPoint='" + measurementPoint + '\'' +
        ", username='" + getUsername() + '\'' +
        ", userId='" + getUserId() + '\'' +
        ", scenario='" + getParentMeasurementPoint() + '\'' +
        ", start=" + getStart() +
        ", end=" + getEnd() +
        ", elapsed=" + getElapsed() +
        '}';
  }
}
