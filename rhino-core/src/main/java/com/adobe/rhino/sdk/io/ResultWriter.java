package com.adobe.rhino.sdk.io;

import com.adobe.rhino.sdk.reporting.LogEvent;
import java.io.Closeable;

/**
 * Simulation result writer logs the benchmark metrics into a flat file, that is the simulation
 * file.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 */
public interface ResultWriter<T extends LogEvent> extends Closeable {

  /**
   * Adds a new log event into the simulation logging source.
   *
   * @param report Log event.
   */
  void report(T report);

  /**
   * Adds a new log event as string into the simulation logging source.
   *
   * @param report Log event.
   */
  void report(String report);
}
