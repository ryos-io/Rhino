package io.ryos.rhino.sdk.runners;

import io.ryos.rhino.sdk.RampupInfo;
import io.ryos.rhino.sdk.SimulationConfig;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rampup {
  private static final Logger LOG = LoggerFactory.getLogger(Rampup.class);

  // start time
  private final Instant startTime;
  private final double slope;
  // timestamp of lastRequest
  private Instant lastRequestTime;
  // getTimeToWait
  private final double startRps;
  private final double targetRps;
  private final Duration duration;

  private static Rampup INSTANCE;

  public Rampup(final Instant startTime, final long startRps, final long targetRps,
      final Duration duration) {
    this.startTime = startTime;
    this.startRps = startRps;
    this.targetRps = targetRps;
    this.duration = duration;
    slope = Math.abs(((double) (targetRps - startRps)) / duration.toSeconds());
  }

  public static synchronized Rampup getInstance() {
    if (INSTANCE == null) {
      RampupInfo rampupInfo = SimulationConfig.getRampupInfo();
      INSTANCE = new Rampup(Instant.now(), rampupInfo.getStartRps(), rampupInfo.getTargetRps(),
          rampupInfo.getDuration());
      return INSTANCE;
    }
    return INSTANCE;
  }

  synchronized public Duration getTimeToWait() {
    return getTimeToWait(Instant::now);
  }

  Duration getTimeToWait(Supplier<Instant> clock) {
    var now = clock.get();
    lastRequestTime = lastRequestTime == null ? now : lastRequestTime;
    // time advanced since beginning
    var virtualTimeAdvanced = Duration.of(
        startTime.until(lastRequestTime, ChronoUnit.MILLIS), ChronoUnit.MILLIS);
    // correct if real time advanced more
    var realTimeAdvanced = Duration.of(
        startTime.until(now, ChronoUnit.MILLIS), ChronoUnit.MILLIS);

    Duration realTimeBonus = Duration.ZERO;
    if (realTimeAdvanced.compareTo(virtualTimeAdvanced) > 0) {
      realTimeBonus = realTimeAdvanced.minus(virtualTimeAdvanced);
      LOG.debug("Oh shit, have to correct time! Correcting next time by {}: ",
          realTimeBonus);
    }

    var tickResult = calcTick(virtualTimeAdvanced, realTimeBonus);
    var rps = tickResult.getKey();
    var tick = tickResult.getValue();
    var delay = virtualTimeAdvanced.minus(realTimeAdvanced).plus(tick);

    if (delay.isNegative()) {
      delay = Duration.ZERO;
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "lastRequestTime={}, advancedMs={}, tick={}, delayMs={}, rps={}",
          lastRequestTime.toEpochMilli(), virtualTimeAdvanced.toMillis(), tick.toMillis(),
          delay.toMillis(), rps);
    }
    lastRequestTime = startTime.plus(virtualTimeAdvanced.plus(realTimeBonus).plus(tick));
    return delay;
  }

  private Map.Entry<Double, Duration> calcTick(final Duration virtualTimeAdvanced,
      final Duration realTimeBonus) {
    double rps = targetRps;
    Duration timeAdvanced = virtualTimeAdvanced.plus(realTimeBonus);
    if (timeAdvanced.compareTo(duration) < 0) {
      rps = startRps + (slope * (timeAdvanced.toMillis() / 1e3D));
    }
    long millis = (long) ((1d / rps) * 1e3D);
    return Map.entry(rps, Duration.ofMillis(millis));
  }
}
