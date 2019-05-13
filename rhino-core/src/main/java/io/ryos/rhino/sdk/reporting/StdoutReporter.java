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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 *
 */
public class StdoutReporter extends AbstractActor {

  private static final long DELAY = 1000L;
  private static final long PERIOD = 1000L; // TODO make configurable.
  private String startTime;
  private String endTime;
  private String numberOfUsers;

  private Timer timer; //TODO shutdown on exit.

  /**
   * Key format is scenario_step_$metric e.g
   * <p>
   *
   * moneyTransfer_checkDebit_responseTime = 15ms
   * moneyTransfer_checkDebit_OK = 250
   * moneyTransfer_checkDebit_NOTFOUND = 2
   */
  private final Map<String, Long> metrics = new HashMap<>();

  public static Props props() {
    return Props.create(StdoutReporter.class, StdoutReporter::new);
  }

  public StdoutReporter() {
    this.timer = new Timer("Stdout Report Timer");

    final TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        flushReport();
      }
    };

    timer.schedule(timerTask, DELAY, PERIOD);
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder.create()
        .match(ScenarioEvent.class, this::persist)
        .build();
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
    metrics.put(responseTypeKey, ++currElapsed);
  }

  private void flushReport() {
    if (metrics.isEmpty()) {
      return;
    }

    final List<String> countMetrics = metrics.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith("Count/"))
        .map(e -> formatKey(e.getKey()) + " " + e.getValue())
        .collect(Collectors.toList());

    final List<String> responseTypeMetrics = metrics.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith("ResponseTime/"))
        .map(e -> formatKey(e.getKey()) + " " + getAvgResponseTime(e.getKey(), e.getValue()))
        .collect(Collectors.toList());

    System.out.println("==================================================");
    System.out.println("-- Metrics ---------------------------------------");
    System.out.println(String.join("\n", countMetrics));
    System.out.println("-- Response Time ---------------------------------");
    System.out.println(String.join("\n", responseTypeMetrics));
    System.out.println("==================================================");
    System.out.println("\n");
  }

  private double getAvgResponseTime(String key, long totalElapsed) {
    final Long totalCount = metrics.get(key.replace("ResponseTime/", "Count/"));
    return totalElapsed / totalCount;
  }

  private String formatKey(String key) {
    return "> " + key
        .replace("ResponseTime/", "")
        .replace("Count/", "");
  }
}
