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
import io.ryos.rhino.sdk.reporting.LogFormatter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Log writer is a result writer implementation creates simulation logs containing simulation
 * metrics.
 *
 * @author <a href="mailto:erhan@ryos.io">Erhan Bagdemir</a>
 */
public class LogWriter extends AbstractActor implements ResultWriter {

  private static final Logger LOG = LogManager.getLogger(LogWriter.class);

  private final LogFormatter logFormatter;

  private Writer writer;

  /**
   * Static factory method to create a new instance of {@link LogFormatter}.
   *
   * @param pathToLogFile Path to the log file.
   * @param logFormatter Log formatter instance.
   * @param <T> A type of {@link LogFormatter}.
   * @return {@link LogFormatter} instance.
   */
  public static <T extends LogFormatter> Props props(final String pathToLogFile, final T logFormatter) {
    return Props.create(LogWriter.class, () -> new LogWriter(pathToLogFile, logFormatter));
  }

  /**
   * Constructs a new {@link LogFormatter} instance.
   *
   * @param logFile Path to the log file.
   * @param formatter Log formatter.
   */
  public LogWriter(final String logFile, final LogFormatter formatter) {
    this.logFormatter = formatter;

    try { // do not close the stream till the load test completes.
      this.writer = new BufferedWriter(new FileWriter(new File(Objects.requireNonNull(logFile))));
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  @Override
  public void report(final LogEvent report) {
    report(logFormatter.format(report));
  }

  @Override
  public void report(final String report) {

    try {
      writer.write(report);
      writer.flush();
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  @Override
  public void close() throws IOException {
    // Nothing to close.
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder.create().match(String.class, this::report
    ).match(LogEvent.class, this::report).build();
  }
}
