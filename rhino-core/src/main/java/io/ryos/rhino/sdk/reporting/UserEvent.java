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
 * @author Erhan Bagdemir
 */
public class UserEvent extends LogEvent {

  private final EventType eventType; // always USER for user events.
  private final String eventStatus; // START or END
  private final String id;

  public UserEvent(String username,
      String userId,
      String parentMeasurement,
      long start,
      long end,
      long elapsed,
      EventType eventType,
      String eventStatus,
      String id) {
    super(username, userId, parentMeasurement, start, end, elapsed);
    this.eventType = eventType;
    this.eventStatus = eventStatus;
    this.id = id;
  }

  public enum EventType {START, END}

  public EventType getEventType() {
    return eventType;
  }

  public String getEventStatus() {
    return eventStatus;
  }

  public String getId() {
    return id;
  }
}
