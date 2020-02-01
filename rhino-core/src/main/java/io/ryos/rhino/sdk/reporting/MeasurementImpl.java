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

import io.ryos.rhino.sdk.reporting.UserEvent.EventType;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import java.util.ArrayList;
import java.util.List;

/**
 * Measurement implementation which measures elapsed time from beginning the execution of the
 * scenario.
 *
 * @author Erhan Bagdemir
 */
public class MeasurementImpl implements Measurement {

  private static final String STR_BLANK = "";
  private final List<LogEvent> events = new ArrayList<>();
  private final String parentName;
  private final String userId;

  private String measurementPoint;
  private boolean cumulativeMeasurement;

  private volatile boolean measurementEnabled;
  private volatile boolean measurementStarted;
  private long start = -1;

  private EventDispatcher dispatcher;

  public MeasurementImpl(final String parentName, final String userId) {
    this(parentName, userId, "", false, true, EventDispatcher.getInstance());
  }

  public MeasurementImpl(final String parentName,
      final String userId,
      final String measurementPoint,
      final boolean cumulativeMeasurement,
      final boolean measurementEnabled,
      final EventDispatcher dispatcher) {

    this.parentName = parentName;
    this.userId = userId;
    this.measurementPoint = measurementPoint;
    this.cumulativeMeasurement = cumulativeMeasurement;
    this.measurementEnabled = measurementEnabled;
    this.dispatcher = dispatcher;
  }

  @Override
  public void start() {

    if (measurementEnabled && !measurementStarted) {
      this.measurementStarted = true;

      // if the start timestamp is not set, then set it. Otherwise, if it is a cumulative
      // measurement, and the start is already set, then skip it.
      if (start < 0 || !cumulativeMeasurement) {
        this.start = System.currentTimeMillis();
      }

      registerStartUserEvent();
    }
  }

  private void registerStartUserEvent() {
    UserEvent userEventStart = new UserEvent(
        STR_BLANK,
        userId,
        parentName,
        start,
        start,
        0L,
        EventType.START,
        STR_BLANK,
        userId
    );

    record(userEventStart);
  }

  @Override
  public void finish() {
    if (!measurementStarted) {
      throw new IllegalStateException("Measurement is not yet started.");
    }

    registerEndUserEvent();
    dispatcher.dispatchEvents(this);
  }

  private void registerEndUserEvent() {
    var elapsed = System.currentTimeMillis() - start;
    UserEvent userEventEnd = new UserEvent(
        STR_BLANK,
        userId,
        parentName,
        start,
        start + elapsed,
        elapsed,
        EventType.END,
        STR_BLANK,
        userId
    );

    record(userEventEnd);
  }

  @Override
  public void measure(String measurement, String status) {

    long startTimer = 0;
    long end = 0;
    long elapsed = 0;

    if (!events.isEmpty()) {
      LogEvent lastEvent = events.get(events.size() - 1);
      end = System.currentTimeMillis();
      startTimer = lastEvent.getEnd();
      elapsed = end - startTimer;
    }

    addEvent(new DslEvent(STR_BLANK, userId, parentName, startTimer, end, elapsed, status, measurement));
  }

  @Override
  public synchronized void record(final LogEvent event) {
    events.add(event);
  }

  @Override
  public void fail(String message) {
    // There is no start event for user measurement, so we need to create one.
    // In Error case, we just want to make the error visible in stdout. We don't actually record
    // any metric here, thus the start/end timestamps are irrelevant.
    if (!measurementEnabled) {
      start();
    }

    // Store the error event in the measurement stack.
    measure(message, "N/A");

    var userEventEnd = new UserEvent(
        STR_BLANK,
        userId,
        parentName,
        start,
        0,
        0L,
        EventType.END,
        STR_BLANK,
        userId
    );

    record(userEventEnd);

    dispatcher.dispatchEvents(this);
  }

  private synchronized void addEvent(final DslEvent event) {
    events.add(event);
  }

  public boolean isLastEvent() {
    if (!events.isEmpty()) {
      var lastEvent = events.get(events.size() - 1);
      return lastEvent instanceof DslEvent;
    }
    return false;
  }

  public List<LogEvent> getEvents() {
    return events;
  }

  public synchronized void purge() {
    events.clear();
  }
}
