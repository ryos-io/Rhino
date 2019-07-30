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
import io.ryos.rhino.sdk.reporting.LogEvent;
import io.ryos.rhino.sdk.reporting.SimulationLogFormatter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Log writer is a result writer implementation creates simulation logs containing simulation
 * metrics.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class SimulationLogWriter extends AbstractActor implements ResultWriter {

  private static final Logger LOG = LogManager.getLogger(SimulationLogWriter.class);

  private SimulationLogFormatter simulationLogFormatter;
  private Writer writer;

  /**
   * Constructs a new {@link SimulationLogFormatter} instance.
   *
   * @param logFile Path to the log file.
   * @param formatter Log formatter.
   */
  public SimulationLogWriter(final String logFile, final SimulationLogFormatter formatter) {
    if (formatter == null) {
      return;
    }

    this.simulationLogFormatter = formatter;

    var pathToLogFile = Objects.requireNonNull(logFile);
    var file = new File(pathToLogFile);

    try { // do not close the stream till the load test completes.
      this.writer = new BufferedWriter(new FileWriter(file));
    } catch (IOException e) {
      LOG.error("Something went wrong while writing to the stream.", e);
      ExceptionUtils.rethrow(e);
    }
  }

  /**
   * Static factory method to create a new instance of {@link SimulationLogFormatter}.
   *
   * @param pathToLogFile Path to the log file.
   * @param logFormatter Log formatter instance.
   * @param <T> A type of {@link SimulationLogFormatter}.
   * @return {@link SimulationLogFormatter} instance.
   */
  public static <T extends SimulationLogFormatter> Props props(final String pathToLogFile,
      final T logFormatter) {
    return Props.create(
        SimulationLogWriter.class, () -> new SimulationLogWriter(pathToLogFile, logFormatter));
  }

  @Override
  public void write(final LogEvent report) {
    if (simulationLogFormatter != null) {
      write(simulationLogFormatter.format(report));
    }
  }

  @Override
  public void write(final String report) {

    try {
      writer.write(report);
      writer.flush();
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  @Override
  public void close() {
    // Nothing to close.
    try {
      writer.close();
    } catch (IOException e) {
      LOG.error("Something went wrong while closing the stream.", e);
      ExceptionUtils.rethrow(e);
    }
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder.create().match(String.class, this::write
    ).match(LogEvent.class, this::write).build();
  }
}
