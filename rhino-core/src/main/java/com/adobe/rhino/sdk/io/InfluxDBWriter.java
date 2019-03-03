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
import java.util.Optional;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.LogLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

/**
 * Writer implementation for Influx DB. It must be activated by using @Influx annotation.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @since 1.1.4
 */
public class InfluxDBWriter extends AbstractActor implements ResultWriter<LogEvent> {

  private static final String RHINO_TEST_DB = "rhino_test_db_";

  public static <T extends InfluxDBWriter> Props props() {
    return Props.create(InfluxDBWriter.class, InfluxDBWriter::new);
  }

  private final InfluxDB influxDB;
  private final String dbName;

  public InfluxDBWriter() {

    this.dbName =
        Optional.ofNullable(SimulationConfig.getInfluxDBName())
            .orElse(RHINO_TEST_DB + System.currentTimeMillis());
    this.influxDB = InfluxDBFactory.connect(SimulationConfig.getInfluxURL());
    this.influxDB.setLogLevel(LogLevel.NONE);

    System.out.println("Creating DB for name: " + dbName);
    this.influxDB.createDatabase(dbName);
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

    BatchPoints batchPoints = BatchPoints
        .database(dbName)
        .tag("async", "true")
        // .retentionPolicy("rhino_retention_policy")
        .consistency(InfluxDB.ConsistencyLevel.ALL)
        .build();

    Point.Builder builder = Point.measurement(report.scenario);
    builder.addField("status", report.status);
    builder.addField("step", report.step);
    builder.addField("pt", report.elapsed);

    Point point = builder.build();
    batchPoints.point(point);

    influxDB.write(batchPoints);
  }

  @Override
  public void report(final String report) {
    System.out.println("string");
  }

  @Override
  public void close() throws IOException {

  }
}
