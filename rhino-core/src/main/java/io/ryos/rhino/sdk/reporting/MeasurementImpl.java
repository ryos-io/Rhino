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

import io.ryos.rhino.sdk.dsl.DslItem;
import io.ryos.rhino.sdk.dsl.DslMethod;
import io.ryos.rhino.sdk.dsl.MeasurableDsl;
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
  private long elapsed = 0L;

  private EventDispatcher dispatcher;

  public MeasurementImpl(final String parentName, final String userId) {
    this(parentName, userId, STR_BLANK, false, true, EventDispatcher.getInstance());
  }

  public MeasurementImpl(final String parentName, final String tagName, final String userId) {
    this(parentName, userId, tagName, false, true, EventDispatcher.getInstance());
  }

  public MeasurementImpl(final String userId, final MeasurableDsl measureableDslItem) {
    this.parentName = getContainerMeasurement(measureableDslItem);
    this.userId = userId;
    this.measurementPoint = measureableDslItem.getMeasurementPoint();
    this.cumulativeMeasurement = measureableDslItem.isCumulative();
    this.measurementEnabled = measureableDslItem.isMeasurementEnabled();
    this.dispatcher = EventDispatcher.getInstance();
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

  private String getContainerMeasurement(final DslItem dslItem) {

    var dsl = dslItem;
    while (dsl.hasParent()) {
      dsl = dsl.getParent();
      if (!dsl.hasParent() && dsl instanceof DslMethod) {
        return dsl.getName();
      }
    }
    return STR_BLANK;
  }

  @Override
  public void start() {
    if (measurementEnabled && !measurementStarted) {
      this.measurementStarted = true;
      this.start = System.currentTimeMillis();
      registerStartUserEvent();
    }
  }

  public void add(final long millis) {
    if (!measurementStarted) {
      throw new IllegalStateException("Measurement is not yet started.");
    }
    this.elapsed += millis;
  }

  public void commit(final String status) {
    commit(this.getMeasurementPoint(), status);
  }

  public void commit(final String measurement, final String status) {
    if (!measurementStarted) {
      throw new IllegalStateException("Measurement is not yet started.");
    }

    addEvent(new DslEvent(STR_BLANK,
        this.userId,
        this.parentName,
        this.start,
        this.start + this.elapsed,
        this.elapsed,
        status,
        measurement));

    var userEventEnd = new UserEvent(STR_BLANK,
        this.userId,
        this.parentName,
        this.start,
        this.start + this.elapsed,
        this.elapsed,
        EventType.END,
        STR_BLANK,
        this.userId
    );

    record(userEventEnd);

    this.dispatcher.dispatchEvents(this);
    this.start = -1;
    this.elapsed = 0;
  }

  private void registerStartUserEvent() {
    UserEvent userEventStart = new UserEvent(
        STR_BLANK,
        this.userId,
        this.parentName,
        this.start,
        this.start,
        0L,
        EventType.START,
        STR_BLANK,
        this.userId
    );

    record(userEventStart);
  }

  @Override
  public void finish() {
    if (!measurementStarted) {
      throw new IllegalStateException("Measurement is not yet started.");
    }

    registerEndUserEvent();
    this.dispatcher.dispatchEvents(this);
    this.start = -1;
  }

  private void registerEndUserEvent() {

    var elapsed = System.currentTimeMillis() - start;
    UserEvent userEventEnd = new UserEvent(STR_BLANK,
        this.userId,
        this.parentName,
        this.start,
        this.start + elapsed,
        elapsed,
        EventType.END,
        STR_BLANK,
        this.userId
    );

    record(userEventEnd);
  }

  @Override
  public long measure(final String measurement, final String status) {
    if (!measurementStarted) {
      throw new IllegalStateException("Measurement is not yet started.");
    }

    long end = System.currentTimeMillis();
    this.elapsed = end - start;

    addEvent(new DslEvent(STR_BLANK,
        this.userId,
        this.parentName,
        this.start,
        end,
        this.elapsed,
        status,
        measurement));

    return this.elapsed;
  }

  @Override
  public long measure(final String status) {
    return measure(measurementPoint, status);
  }

  @Override
  public synchronized void record(final LogEvent event) {
    events.add(event);
  }

  @Override
  public void fail(final String message) {
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
        this.userId,
        this.parentName,
        this.start,
        0,
        0L,
        EventType.END,
        STR_BLANK,
        this.userId
    );

    record(userEventEnd);

    dispatcher.dispatchEvents(this);
  }

  private synchronized void addEvent(final DslEvent event) {
    events.add(event);
  }

  public List<LogEvent> getEvents() {
    return events;
  }

  public synchronized void purge() {
    events.clear();
  }

  public String getParentName() {
    return parentName;
  }

  public String getMeasurementPoint() {
    return measurementPoint;
  }

  public boolean isCumulativeMeasurement() {
    return cumulativeMeasurement;
  }

  public boolean isMeasurementEnabled() {
    return measurementEnabled;
  }

  public boolean isMeasurementStarted() {
    return measurementStarted;
  }
}
