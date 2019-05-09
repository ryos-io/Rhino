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

package io.ryos.rhino.sdk.io;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.reporting.LogEvent;
import io.ryos.rhino.sdk.reporting.SimulationEvent;
import java.util.Optional;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.LogLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

/**
 * Writer implementation for Influx DB. It must be activated by using @Influx annotation.
 *
 * @author <a href="mailto:erhan@ryos.io">Erhan Bagdemir</a>
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
  public void close() {
    // Intentionally left empty.
  }
}
