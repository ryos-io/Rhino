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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Stdout reporter outputs the current status of the test run. It gives out information like number
 * of requests per scenario, and avg. response times.
 * <p>
 *
 * <a href="mailto:erhan@ryos.io">Erhan Bagdemir</a>
 */
public class StdoutReporter extends AbstractActor {

  private static final long DELAY = 1000L;
  private static final long PERIOD = 1000L * 5; // TODO make configurable.
  private static final String BORDER_LINE_BOLD =
      "==========================================================================";
  private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
  private static final String NOT_AVAILABLE = "N/A";
  public static final int MSG_OK = 200;

  private Instant startTime;
  private int duration;
  private int numberOfUsers;
  private boolean terminated;

  private Timer timer;

  /**
   * Key format is scenario_step_$metric e.g
   * <p>
   *
   * moneyTransfer_checkDebit_responseTime = 15ms moneyTransfer_checkDebit_OK = 250
   * moneyTransfer_checkDebit_NOTFOUND = 2
   */
  private final Map<String, Long> metrics = new HashMap<>();

  // Akka static factory.
  public static Props props(int numberOfUsers, Instant startTime, int duration) {
    return Props.create(StdoutReporter.class, () -> new StdoutReporter(numberOfUsers, startTime,
        duration));
  }

  private StdoutReporter(int numberOfUsers, Instant startTime, int duration) {
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
    timer.schedule(timerTask, DELAY, PERIOD);
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder.create()
        .match(ScenarioEvent.class, this::persist)
        .match(EndTestEvent.class, this::endTest)
        .build();
  }

  private void endTest(final EndTestEvent endEvent) {
    if (terminated) {
      sender().tell(MSG_OK, self());
      return;
    }

    this.timer.cancel();
    flushReport(endEvent);
    sender().tell(MSG_OK, self());
    this.terminated = true;
  }

  private void persist(final ScenarioEvent logEvent) {

    String countKey = String.format("Count/%s/%s/%s",
        logEvent.scenario,
        logEvent.step,
        logEvent.status);

    String responseTypeKey = String.format("ResponseTime/%s/%s/%s",
        logEvent.scenario,
        logEvent.step,
        logEvent.status);

    if (!metrics.containsKey(countKey)) {
      metrics.put(countKey, 0L);
    }

    if (!metrics.containsKey(responseTypeKey)) {
      metrics.put(responseTypeKey, 0L);
    }

    Long currVal = metrics.get(countKey);
    metrics.put(countKey, ++currVal);

    Long currElapsed = metrics.get(responseTypeKey);
    metrics.put(responseTypeKey, currElapsed + logEvent.elapsed);
  }

  private void flushReport(EndTestEvent event) {
    if (metrics.isEmpty()) {
      System.out.println("There was no measurement in Recorder. Test is still running...");
      return;
    }

    List<String> countMetrics = metrics.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith("Count/"))
        .map(e -> formatKey(e.getKey()) + " " + e.getValue())
        .collect(Collectors.toList());

    List<String> responseTypeMetrics = metrics.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith("ResponseTime/"))
        .map(
            e -> formatKey(e.getKey()) + " " + getAvgResponseTime(e.getKey(), e.getValue()) + " ms")
        .collect(Collectors.toList());

    long overAllResponseTime = metrics.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith("ResponseTime/"))
        .map(Entry::getValue)
        .reduce(Long::sum).orElse(0L);

    long totalNumberOfRequests = metrics.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith("Count/"))
        .map(Entry::getValue)
        .reduce(Long::sum).orElse(0L);

    long avgRT = overAllResponseTime / totalNumberOfRequests;

    StringBuilder output = new StringBuilder();
    output.append("Number of users logged in : ").append(numberOfUsers).append('\n');
    output.append("Tests started : ").append(formatDate(startTime)).append('\n');
    output.append("Elapsed : ").append(Duration.between(startTime, Instant.now()).toSeconds())
        .append(" secs ETA : ")
        .append(formatDate(startTime.plus(duration, ChronoUnit.MINUTES)))
        .append(" (duration ")
        .append(duration)
        .append(" mins)")
        .append('\n');

    if (event != null) {
      output.append("Tests ended : ")
          .append(formatDate(event.getEndTestTime()))
          .append('\n');
    }
    output.append(BORDER_LINE_BOLD).append('\n');
    output.append("-- Number of executions --------------------------------------------------")
        .append('\n');
    output.append(String.join("\n", countMetrics)).append('\n');
    output.append("-- Response Time ---------------------------------------------------------")
        .append('\n');
    output.append(String.join("\n", responseTypeMetrics)).append('\n').append('\n');
    output.append(BORDER_LINE_BOLD).append('\n');
    output.append(String.format("%50s %9s ms", "Average Response Time", avgRT)).append('\n');
    output.append(String.format("%50s %9s ", "Total Request", totalNumberOfRequests)).append('\n');
    output.append(BORDER_LINE_BOLD).append('\n');

    if (event != null) {
      output.append("\n").append("Bye!").append('\n');
    }
    System.out.println(output.toString());
  }

  private String formatDate(Instant dateTime) {
    if (dateTime == null) {
      return NOT_AVAILABLE;
    }
    return DateTimeFormatter.ofPattern(DATETIME_PATTERN).withZone(ZoneId.systemDefault())
        .format(dateTime);
  }

  private long getAvgResponseTime(String key, long totalElapsed) {
    final Long totalCount = metrics.get(key.replace("ResponseTime/", "Count/"));
    return totalElapsed / totalCount;
  }

  private String formatKey(String key) {

    String normalizedStr = key
        .replace("ResponseTime/", "")
        .replace("Count/", "");

    String[] split = normalizedStr.split("/");

    return String.format("> %-15.15s  %-15.15s %25s", split);
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
