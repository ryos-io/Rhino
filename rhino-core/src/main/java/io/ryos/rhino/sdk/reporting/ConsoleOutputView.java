/*
 * Copyright 2018 Ryos.io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ryos.rhino.sdk.reporting;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleOutputView {

  private static final String COUNT = "Count/";
  private static final String RESPONSE_TIME = "ResponseTime/";
  private static final String BLANK_STR = "";
  private static final Logger LOG = LoggerFactory.getLogger(ConsoleOutputView.class);
  private static final String DATETIME_PATTERN = "HH:mm:ss";
  private static final String NOT_AVAILABLE = "N/A";
  private static final String BORDER_LINE_BOLD =
      "==========================================================================";

  private final int containerWidth;
  private final int sizeContainerDSL;
  private final int sizeMeasurement;

  private final int numberOfUsers;
  private final Instant startTime;
  private final Instant endTime;
  private final Duration duration;
  private final Map<String, Long> metrics;

  public ConsoleOutputView(int containerWidth, int sizeContainerDSL, int sizeMeasurement,
      int numberOfUsers, Instant startTime, Instant endTime, Duration duration,
      Map<String, Long> metrics) {
    this.containerWidth = containerWidth;
    this.sizeContainerDSL = sizeContainerDSL;
    this.sizeMeasurement = sizeMeasurement;
    this.numberOfUsers = numberOfUsers;
    this.startTime = startTime;
    this.endTime = endTime;
    this.duration = duration;
    this.metrics = metrics;
  }

  public String getView() {
    if (metrics.isEmpty()) {
      LOG.info("There is no record in measurement yet. Test is running...");
      return "";
    }

    var countMetrics = metrics.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(COUNT))
        .map(e -> formatKey(e.getKey()) + " " + e.getValue())
        .collect(Collectors.toList());

    var responseTypeMetrics = metrics.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(RESPONSE_TIME))
        .map(
            e -> formatKey(e.getKey()) + " " + getAvgResponseTime(e.getKey(), e.getValue()) + " ms")
        .collect(Collectors.toList());

    long overAllResponseTime = metrics.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(RESPONSE_TIME))
        .map(Entry::getValue)
        .reduce(Long::sum).orElse(0L);

    long totalNumberOfRequests = metrics.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(COUNT))
        .map(Entry::getValue)
        .reduce(Long::sum).orElse(0L);

    long avgRT = -1;
    if (totalNumberOfRequests > 0) {
      avgRT = overAllResponseTime / totalNumberOfRequests;
    }

    StringBuilder output = new StringBuilder();
    if (numberOfUsers > 0) {
      output.append("Number of users logged in : ").append(numberOfUsers).append('\n');
    }
    output.append("Tests started : ").append(formatDate(startTime)).append('\n');
    output.append("Elapsed : ").append(Duration.between(startTime, Instant.now()).toSeconds())
        .append(" secs ETA : ")
        .append(formatDate(startTime.plus(duration)))
        .append(" (duration ")
        .append(duration.toMinutes())
        .append(" mins)")
        .append('\n');

    if (endTime != null) {
      output.append("Tests ended : ")
          .append(formatDate(endTime))
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

    return output.toString();
  }

  private String formatDate(Instant dateTime) {
    if (dateTime == null) {
      return NOT_AVAILABLE;
    }
    return DateTimeFormatter.ofPattern(DATETIME_PATTERN).withZone(ZoneId.systemDefault())
        .format(dateTime);
  }

  private long getAvgResponseTime(String key, long totalElapsed) {
    final Long totalCount = metrics.get(key.replace(RESPONSE_TIME, COUNT));
    if (totalCount > 0) {
      return totalElapsed / totalCount;
    }
    return -1;
  }

  private String formatKey(String key) {
    var normalizedStr = key.replace(RESPONSE_TIME, BLANK_STR)
        .replace(COUNT, BLANK_STR);
    var sections = normalizedStr.split("/");
    if (sections.length > 2) {
      return String.format("> %-15.15s  %-15.15s %25s", sections[0], sections[1], sections[2]);
    }

    return BLANK_STR;
  }
}
