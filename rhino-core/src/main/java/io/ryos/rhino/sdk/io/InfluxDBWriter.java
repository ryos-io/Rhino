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
import io.ryos.rhino.sdk.reporting.DslEvent;
import io.ryos.rhino.sdk.reporting.LogEvent;
import io.ryos.rhino.sdk.reporting.UserEvent;
import io.ryos.rhino.sdk.reporting.UserEvent.EventType;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

/**
 * Writer implementation for Influx DB. It must be activated by using @Influx annotation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class InfluxDBWriter extends AbstractActor implements ResultWriter<LogEvent> {

  private static final String DEFAULT_DB = "rhino_test_db_";

  private final InfluxDB influxDB;
  private final String dbName;

  public static Props props() {
    return Props.create(InfluxDBWriter.class, InfluxDBWriter::new);
  }

  private InfluxDBWriter() {
    this.dbName = Optional.ofNullable(SimulationConfig.getInfluxDBName())
        .orElse(DEFAULT_DB + System.currentTimeMillis());

    var client = new OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .retryOnConnectionFailure(true);

    if (StringUtils.isEmpty(SimulationConfig.getInfluxUsername())) {
      this.influxDB = InfluxDBFactory.connect(SimulationConfig.getInfluxURL(), client)
          .setRetentionPolicy(SimulationConfig.getInfluxRetentionPolicy())
          .setConsistency(ConsistencyLevel.ONE)
          .enableBatch(SimulationConfig.getInfluxBatchActions(),
              SimulationConfig.getInfluxBatchDuration(),
              TimeUnit.MICROSECONDS);
    } else {
      this.influxDB = InfluxDBFactory.connect(SimulationConfig.getInfluxURL(),
          SimulationConfig.getInfluxUsername(),
          SimulationConfig.getInfluxPassword(), client)
          .setRetentionPolicy(SimulationConfig.getInfluxRetentionPolicy())
          .setConsistency(ConsistencyLevel.ONE)
          .enableBatch(SimulationConfig.getInfluxBatchActions(),
              SimulationConfig.getInfluxBatchDuration(),
              TimeUnit.MICROSECONDS);
    }

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

    if (logEvent instanceof DslEvent) {
      createPoint((DslEvent) logEvent);
    }

    if (logEvent instanceof UserEvent) {
      createPoint((UserEvent) logEvent);
    }
  }

  private void createPoint(DslEvent report) {
    var builder = Point.measurement("simulation_" + SimulationConfig.getSimulationId())
        .tag("step", report.getMeasurementPoint())
        .tag("status", report.getStatus())
        .addField("scenario", report.getParentMeasurementPoint())
        .addField("pt", report.getElapsed())
        .addField("node", SimulationConfig.getNode());
    influxDB.setDatabase(dbName);
    influxDB.write(builder.build());
  }

  private void createPoint(UserEvent report) {
    if (report.getEventType() == EventType.END) {
      var builder =
          Point.measurement("user_" + SimulationConfig.getSimulationId())
              .tag("scenario", report.getParentMeasurementPoint())
              .addField("id", report.getId())
              .addField("node", SimulationConfig.getNode())
              .addField("pt", report.getElapsed());
      influxDB.setDatabase(dbName);
      influxDB.write(builder.build());
    }
  }

  @Override
  public void write(final String report) {
    // Not implemented.
  }

  @Override
  public void close() {
    // Intentionally left empty.
  }
}
