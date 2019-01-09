package com.adobe.rhino.sdk.io;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.adobe.rhino.sdk.reporting.LogEvent;
import com.adobe.rhino.sdk.reporting.LogFormatter;
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
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 */
public class LogWriter extends AbstractActor implements ResultWriter {

  private static final Logger LOG = LogManager.getLogger(LogWriter.class);

  private Writer writer;

  private LogFormatter logFormatter;

  public static <T extends LogFormatter> Props props(final String pathToLogFile,
      final T logFormatter) {
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
    try {
      writer.write(logFormatter.format(report));
      writer.flush();
    } catch (IOException e) {
      LOG.error(e);
    }
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
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder.create().match(String.class, this::report
    ).match(LogEvent.class, this::report).build();
  }
}
