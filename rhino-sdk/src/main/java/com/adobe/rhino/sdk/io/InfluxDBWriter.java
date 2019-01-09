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

package com.adobe.rhino.sdk.io;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.adobe.rhino.sdk.SimulationConfig;
import com.adobe.rhino.sdk.reporting.LogEvent;
import com.adobe.rhino.sdk.reporting.SimulationEvent;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;

/**
 * Writer implementation for Influx DB. It must be activated by using @Influx annotation.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @since 1.1.4
 */
public class InfluxDBWriter extends AbstractActor implements ResultWriter<LogEvent> {

  public static <T extends InfluxDBWriter> Props props() {
    return Props.create(InfluxDBWriter.class, InfluxDBWriter::new);
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder.create()
        .match(String.class, this::report)
        .match(LogEvent.class, this::report)
        .build();
  }

  @Override
  public void report(final LogEvent logEvent) {

    SimulationEvent report;
    if (logEvent instanceof SimulationEvent) {
      report = (SimulationEvent) logEvent;
    } else {
      return;
    }

    var writeQuery = String.format("%s,status=%s,step=%s pt=%d",
        report.scenario,
        report.status,
        report.step.replace(" ", "\\ "),
        report.elapsed);

    var client = HttpClient.newHttpClient();
    var request = HttpRequest.newBuilder()
        .uri(URI.create(
            SimulationConfig.getInfluxURL() + "/write?db=" + SimulationConfig.getInfluxDBName()))
        .POST(BodyPublishers.ofString(writeQuery))
        .build();

    try {
      final CompletableFuture<HttpResponse<String>> httpResponseCompletableFuture = client
          .sendAsync(request, BodyHandlers.ofString());
    } catch (Throwable t) {
      System.out.println("ERROR: " + t.getMessage());
    }
  }

  @Override
  public void report(final String report) {
    System.out.println("string");
  }

  @Override
  public void close() throws IOException {

  }
}
