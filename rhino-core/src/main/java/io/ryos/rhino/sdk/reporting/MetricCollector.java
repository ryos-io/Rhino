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
import io.ryos.rhino.sdk.ExecutionMode;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The actor outputs the current status of the test run. It gives out information to stdout like
 * number of requests per scenario, and avg. response times.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class MetricCollector extends AbstractActor {
  private static final long DELAY = 1000L;
  private static final long PERIOD = 1000L * 5; // TODO make configurable.
  private static final int MSG_OK = 200;
  private static final int CONTAINER_WIDTH = 130;

  private Instant startTime;
  private Duration duration;
  private int numberOfUsers;
  private ExecutionMode executionMode;

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

  private final Map<String, String> verificationResult = new LinkedHashMap<>();
  private final Map<String, Long> performanceMetrics = new LinkedHashMap<>();
  private final Map<String, SummaryStatistics> performanceStats = new LinkedHashMap<>();
  private final Map<String, DescriptiveStatistics> performanceRollingStats = new LinkedHashMap<>();

  public static Props props(int numberOfUsers, Instant startTime, Duration duration) {
    return Props.create(MetricCollector.class, () -> new MetricCollector(numberOfUsers, startTime,
        duration));
  }

  private MetricCollector(final int numberOfUsers, final Instant startTime, final Duration duration,
      final ExecutionMode executionMode) {

    this.executionMode = executionMode;
    this.receivedTerminationEvent = false;
    this.duration = duration;
    this.numberOfUsers = numberOfUsers;
    this.startTime = startTime;
    this.timer = new Timer("Stdout Report Timer");

    if (this.executionMode != ExecutionMode.VERIFY) {
      startTimer();
    }
  }

  private MetricCollector(final int numberOfUsers, final Instant startTime,
      final Duration duration) {
    this(numberOfUsers, startTime, duration, ExecutionMode.PERFORMANCE);
  }

  private void startTimer() {

    final TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        flushReport(null);
      }
    };
    timer.schedule(timerTask, DELAY, PERIOD);
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

    var verificationTypeKey = String.format("Verification/%s/%s/%s",
        logEvent.getParentMeasurementPoint(),
        logEvent.getMeasurementPoint(),
        logEvent.getStatus());

    if (!performanceStats.containsKey(responseTypeKey)) {
      performanceStats.put(responseTypeKey, new SummaryStatistics());
    }

    if (!performanceRollingStats.containsKey(responseTypeKey)) {
      var descriptiveStatistics = new DescriptiveStatistics();
      descriptiveStatistics.setWindowSize(100);
      performanceRollingStats.put(responseTypeKey, descriptiveStatistics);
    }

    if (!performanceMetrics.containsKey(countKey)) {
      performanceMetrics.put(countKey, 0L);
    }

    if (!performanceMetrics.containsKey(responseTypeKey)) {
      performanceMetrics.put(responseTypeKey, 0L);
    }

    if (!verificationResult.containsKey(verificationTypeKey)) {
      verificationResult.put(responseTypeKey, "");
    }

    VerificationInfo verificationInfo = logEvent.getVerificationInfo();
    if (verificationInfo != null) {
      boolean testResult = verificationInfo.getPredicate().test(logEvent.getStatus());
      verificationResult.put(verificationTypeKey,
          getVerificationResult(testResult) + (!testResult ?
              "  Expected " + verificationInfo.getDescription() + " but was " + logEvent.getStatus()
              : ""));
    }

    var currVal = performanceMetrics.get(countKey);
    performanceMetrics.put(countKey, ++currVal);

    var currElapsed = performanceMetrics.get(responseTypeKey);
    performanceMetrics.put(responseTypeKey, currElapsed + logEvent.getElapsed());

    performanceStats.get(responseTypeKey).addValue(logEvent.getElapsed());
    performanceRollingStats.get(responseTypeKey).addValue(logEvent.getElapsed());
  }

  private String getVerificationResult(Boolean testResult) {
    return testResult ? "SUCCESS" : "FAIL";
  }

  private void flushReport(EndTestEvent event) {
    if (performanceMetrics.isEmpty()) {
      System.out.println("There is no record in measurement yet. Test is running...");
      return;
    }

    var consoleOutputView = new PerformanceConsoleOutputView(CONTAINER_WIDTH,
        numberOfUsers,
        startTime,
        event != null ? event.getEndTestTime() : null,
        duration,
        verificationResult,
        performanceMetrics,
        performanceStats,
        performanceRollingStats);


        System.out.println(consoleOutputView.getView());
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
