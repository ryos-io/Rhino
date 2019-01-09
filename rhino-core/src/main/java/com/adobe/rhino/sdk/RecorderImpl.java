/**************************************************************************
 *                                                                        *
 * ADOBE CONFIDENTIAL                                                     *
 * ___________________                                                    *
 *                                                                        *
 *  Copyright 2018 Adobe Systems Incorporated                             *
 *  All Rights Reserved.                                                  *
 *                                                                        *
 * NOTICE:  All information contained herein is, and remains              *
 * the property of Adobe Systems Incorporated and its suppliers,          *
 * if any.  The intellectual and technical concepts contained             *
 * herein are proprietary to Adobe Systems Incorporated and its           *
 * suppliers and are protected by trade secret or copyright law.          *
 * Dissemination of this information or reproduction of this material     *
 * is strictly forbidden unless prior written permission is obtained      *
 * from Adobe Systems Incorporated.                                       *
 **************************************************************************/

package com.adobe.rhino.sdk;

import static com.fasterxml.jackson.core.JsonToken.NOT_AVAILABLE;

import com.adobe.rhino.sdk.reporting.LogEvent;
import com.adobe.rhino.sdk.reporting.SimulationEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
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
