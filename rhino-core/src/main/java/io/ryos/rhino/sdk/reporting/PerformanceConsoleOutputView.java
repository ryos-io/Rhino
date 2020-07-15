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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceConsoleOutputView {

  private static final String VERIFICATION = "Verification/";
  private static final String COUNT = "Count/";
  private static final String RESPONSE_TIME = "ResponseTime/";
  private static final String BLANK_STR = "";
  private static final int HEADER_LEFT_PADDING_SIZE = 1;
  private static final Logger LOG = LoggerFactory.getLogger(PerformanceConsoleOutputView.class);
  private static final String DATETIME_PATTERN = "HH:mm:ss";
  private static final String NOT_AVAILABLE = "N/A";
  private static final String EMPTY_SPACE = " ";
  private static final String BORDER_LINE_STYLE = "=";
  private static final String SPLITTER = "/";
  private static final char LB = '\n';
  private static final String HEADER_LINE_STYLE = "-";

  private final int containerWidth;
  private final int numberOfUsers;
  private final Instant startTime;
  private final Instant endTime;
  private final Duration duration;
  private final Map<String, String> verification;
  private final Map<String, Long> metrics;
  private final Map<String, SummaryStatistics> stats;
  private final Map<String, DescriptiveStatistics> rollingStats;

  public PerformanceConsoleOutputView(int containerWidth,
      int numberOfUsers, Instant startTime, Instant endTime, Duration duration,
      Map<String, String> verification,
      Map<String, Long> metrics,
      Map<String, SummaryStatistics> stats,
      Map<String, DescriptiveStatistics> rollingStats) {
    this.containerWidth = containerWidth;
    this.numberOfUsers = numberOfUsers;
    this.startTime = startTime;
    this.endTime = endTime;
    this.duration = duration;
    this.metrics = metrics;
    this.stats = stats;
    this.rollingStats = rollingStats;
    this.verification = verification;
  }

  public String getView() {
    if (metrics.isEmpty()) {
      LOG.info("There is no record in measurement yet. Test is running...");
      return "";
    }

    var verificationResults = verification.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(VERIFICATION))
        .map(e -> formatVerifyKey(e.getKey()) + EMPTY_SPACE + String.format("%5s",
            e.getValue()))
        .collect(Collectors.toList());

    var countMetrics = metrics.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(COUNT))
        .map(e -> formatKey(e.getKey()) + EMPTY_SPACE + String.format("%5s", Math.round(e.getValue())))
        .collect(Collectors.toList());

    var responseTimeStats = stats.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(RESPONSE_TIME))
        .map(e -> formatKey(e.getKey()) + EMPTY_SPACE + String.format("%5s ms", Math.round(e.getValue().getMean())))
        .collect(Collectors.toList());

    var responseTimeRollingStats = rollingStats.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(RESPONSE_TIME))
        .map(e -> formatKey(e.getKey()) + EMPTY_SPACE
            + String.format("%5s ms %5s ms %5s ms %5s ms",
            Math.round(e.getValue().getMean()),
            Math.round(e.getValue().getPercentile(50.0)),
            Math.round(e.getValue().getPercentile(96.0)),
            Math.round(e.getValue().getPercentile(99.0)))
        )
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
      output.append("Number of users logged in : ").append(numberOfUsers).append(LB);
    }
    output.append("Tests started : ").append(formatDate(startTime)).append(LB);
    output.append("Elapsed : ").append(Duration.between(startTime, Instant.now()).toSeconds())
        .append(" secs ETA : ")
        .append(formatDate(startTime.plus(duration)))
        .append(LB);

    if (endTime != null) {
      output.append("Tests ended : ")
          .append(formatDate(endTime))
          .append(LB);
    }
    output.append(BORDER_LINE_STYLE.repeat(containerWidth)).append(LB);
    output.append(createHeader("Number of executions")).append(LB);
    output.append(String.join("\n", countMetrics)).append(LB);
    output.append(createHeader("Response Time (overall avg)")).append(LB);
    output.append(String.join("\n", responseTimeStats)).append(LB);
    output.append(createHeader("Verification")).append(LB);
    output.append(String.join("\n", verificationResults)).append(LB).append(LB);
    output.append(createHeader("Rolling Stats (100 sample-window)")).append(LB);
    output.append(String.format("%90s %5s %8s %8s %8s", "status", "mean", "median", "p96", "p99")).append(LB);
    output.append(HEADER_LINE_STYLE.repeat(containerWidth)).append(LB);
    output.append(String.join("\n", responseTimeRollingStats)).append(LB).append(LB);
    output.append(BORDER_LINE_STYLE.repeat(containerWidth)).append(LB);
    output.append(String.format("%70s %25.9s ms", "Average Response Time", avgRT)).append(LB);
    output.append(String.format("%70s %19.9s ", "Total Request", totalNumberOfRequests)).append(LB);
    output.append(BORDER_LINE_STYLE.repeat(containerWidth)).append(LB);

    return output.toString();
  }

  private String createHeader(final String text) {
    return HEADER_LINE_STYLE.repeat(HEADER_LEFT_PADDING_SIZE) + EMPTY_SPACE + text + EMPTY_SPACE
        + HEADER_LINE_STYLE
        .repeat(containerWidth - 2 - text.length() - HEADER_LEFT_PADDING_SIZE);
  }

  private String formatDate(final Instant dateTime) {
    if (dateTime == null) {
      return NOT_AVAILABLE;
    }
    return DateTimeFormatter
        .ofPattern(DATETIME_PATTERN)
        .withZone(ZoneId.systemDefault())
        .format(dateTime);
  }

  private String formatVerifyKey(final String key) {
    var normalizedStr = key
        .replace(VERIFICATION, BLANK_STR);
    var sections = normalizedStr.split(SPLITTER);
    if (sections.length > 2) {
      return String.format("> %-38.39s%-46.57s", sections[0], sections[1]);
    }
    return BLANK_STR;
  }

  private String formatKey(final String key) {
    var normalizedStr = key
        .replace(RESPONSE_TIME, BLANK_STR)
        .replace(VERIFICATION, BLANK_STR)
        .replace(COUNT, BLANK_STR);
    var sections = normalizedStr.split(SPLITTER);
    if (sections.length > 2) {
      return String.format("> %-38.39s%-38.39s%12.12s", sections[0], sections[1], sections[2]);
    }
    return BLANK_STR;
  }
}
