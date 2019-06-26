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
import io.ryos.rhino.sdk.reporting.ScenarioEvent;
import io.ryos.rhino.sdk.reporting.UserEvent;
import io.ryos.rhino.sdk.reporting.UserEvent.EventType;
import java.util.Optional;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDB.LogLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

/**
 * Writer implementation for Influx DB. It must be activated by using @Influx annotation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class InfluxDBWriter extends AbstractActor implements ResultWriter<LogEvent> {

  private static final String RHINO_TEST_DB = "rhino_test_db_";
  private final InfluxDB influxDB;
  private final String dbName;

  public static <T extends InfluxDBWriter> Props props() {
    return Props.create(InfluxDBWriter.class, InfluxDBWriter::new);
  }

  public InfluxDBWriter() {
    this.dbName = Optional.ofNullable(SimulationConfig.getInfluxDBName())
        .orElse(RHINO_TEST_DB + System.currentTimeMillis());

    this.influxDB = InfluxDBFactory.connect(SimulationConfig.getInfluxURL())
        .setLogLevel(LogLevel.NONE)
        .setConsistency(ConsistencyLevel.ONE);

    influxDB.createDatabase(dbName);
  }

  @Override
  public Receive createReceive() {

    return ReceiveBuilder.create()
        .match(String.class, this::write)
        .match(LogEvent.class, this::write)
        .build();
  }

  @Override
  public void write(final LogEvent logEvent) {

    if (logEvent instanceof ScenarioEvent) {
      createPoint((ScenarioEvent) logEvent);
    }

    if (logEvent instanceof UserEvent) {
      createPoint((UserEvent) logEvent);
    }
  }

  private void createPoint(ScenarioEvent report) {
    var batchPoints = BatchPoints
        .database(dbName)
        .consistency(InfluxDB.ConsistencyLevel.ALL)
        .build();

    var builder = Point.measurement("simulation_" + SimulationConfig.getSimulationId())
        .tag("step", report.step)
        .addField("scenario", report.scenario)
        .addField("status", report.status)
        .addField("pt", report.elapsed)
        .addField("node", SimulationConfig.getNode());

    batchPoints.point(builder.build());
    influxDB.write(batchPoints);
  }

  private void createPoint(UserEvent report) {
    if (report.eventType == EventType.END) {
      var batchPoints = BatchPoints
          .database(dbName)
          .consistency(InfluxDB.ConsistencyLevel.ALL)
          .build();

      var builder =
          Point.measurement("user_" + SimulationConfig.getSimulationId())
              .tag("scenario", report.scenario)
              .addField("id", report.id)
              .addField("node", SimulationConfig.getNode())
              .addField("pt", report.elapsed);
      batchPoints.point(builder.build());
      influxDB.write(batchPoints);
    }
  }

  @Override
  public void write(final String report) {
    System.out.println("string");
  }

  @Override
  public void close() {
    // Intentionally left empty.
  }
}
