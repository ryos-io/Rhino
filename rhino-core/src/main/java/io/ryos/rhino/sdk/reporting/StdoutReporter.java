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

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The actor outputs the current status of the test run. It gives out information to stdout like
 * number of requests per scenario, and avg. response times.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class StdoutReporter extends AbstractActor {

  private static final Logger LOG = LoggerFactory.getLogger(StdoutReporter.class);
  private static final long DELAY = 1000L;
  private static final long PERIOD = 1000L * 5; // TODO make configurable.
  private static final int MSG_OK = 200;

  private Instant startTime;
  private Duration duration;
  private int numberOfUsers;

  /**
   * Indicates whether the termination event has been received.
   * <p>
   */
  private volatile boolean receivedTerminationEvent;

  /**
   * The timer for flushing output.
   * <p>
   */
  private Timer timer;

  /**
   * Key format is scenario_step_$metric e.g
   * <p>
   * <p>
   * moneyTransfer_checkDebit_responseTime = 15ms moneyTransfer_checkDebit_OK = 250
   * moneyTransfer_checkDebit_NOTFOUND = 2
   */
  private final Map<String, Long> metrics = new LinkedHashMap<>();

  // Akka static factory.
  public static Props props(int numberOfUsers, Instant startTime, Duration duration) {
    return Props.create(StdoutReporter.class, () -> new StdoutReporter(numberOfUsers, startTime,
        duration));
  }

  private StdoutReporter(int numberOfUsers, Instant startTime, Duration duration) {
    this.receivedTerminationEvent = false;
    this.duration = duration;
    this.numberOfUsers = numberOfUsers;
    this.startTime = startTime;
    this.timer = new Timer("Stdout Report Timer");
    final TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        flushReport(null);
      }
    };
    this.timer.schedule(timerTask, DELAY, PERIOD);
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder.create()
        .match(DslEvent.class, this::persist)
        .match(EndTestEvent.class, this::activateTermination)
        .build();
  }

  private void activateTermination(final EndTestEvent endEvent) {
    if (receivedTerminationEvent) {
      sender().tell(MSG_OK, self());
      return;
    }

    flushReport(endEvent);
    this.receivedTerminationEvent = true;
    this.timer.cancel();
    sender().tell(MSG_OK, self());
  }

  private void persist(final DslEvent logEvent) {
    var countKey = String.format("Count/%s/%s/%s",
        logEvent.getParentMeasurementPoint(),
        logEvent.getMeasurementPoint(),
        logEvent.getStatus());

    var responseTypeKey = String.format("ResponseTime/%s/%s/%s",
        logEvent.getParentMeasurementPoint(),
        logEvent.getMeasurementPoint(),
        logEvent.getStatus());

    if (!metrics.containsKey(countKey)) {
      metrics.put(countKey, 0L);
    }

    if (!metrics.containsKey(responseTypeKey)) {
      metrics.put(responseTypeKey, 0L);
    }

    var currVal = metrics.get(countKey);
    metrics.put(countKey, ++currVal);

    var currElapsed = metrics.get(responseTypeKey);
    metrics.put(responseTypeKey, currElapsed + logEvent.getElapsed());
  }

  private void flushReport(EndTestEvent event) {
    if (metrics.isEmpty()) {
      LOG.info("There is no record in measurement yet. Test is running...");
      return;
    }

    var consoleOutputView = new ConsoleOutputView(100,
        numberOfUsers,
        startTime,
        event != null ? event.getEndTestTime() : null,
        duration,
        metrics);

    if (LOG.isInfoEnabled()) {
      LOG.info(consoleOutputView.getView());
    }
  }

  public static class EndTestEvent {

    private final Instant endTestTime;

    public EndTestEvent(final Instant endTestTime) {
      this.endTestTime = endTestTime;
    }

    public Instant getEndTestTime() {
      return endTestTime;
    }
  }
}
