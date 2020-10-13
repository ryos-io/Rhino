/*************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2020 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 **************************************************************************/
package io.ryos.rhino.sdk.runners;

import io.ryos.rhino.sdk.RampupInfo;
import io.ryos.rhino.sdk.SimulationConfig;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class Rampup {
  private static final Logger LOG = LoggerFactory.getLogger(Rampup.class);

  // start time
  private final Instant startTime;
  private final double slope;
  // timestamp of lastRequest
  private Instant lastRequestTime;
  // getTimeToWait
  private final long startRps;
  private final long targetRps;
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

  public synchronized Duration getTimeToWait() {
    Instant now = Instant.now();
    long durationSec = startTime.until(now, ChronoUnit.SECONDS);
    long rps = targetRps;
    if (durationSec <= duration.toSeconds()) {
      rps = (long) Math.floor(startRps + slope * durationSec);
    }
    if (lastRequestTime == null) {
      lastRequestTime = now;
      return Duration.ZERO;
    }
    // else
    Duration timeAdvanced = Duration.ofMillis(lastRequestTime.until(now, ChronoUnit.MILLIS));
    Duration nextTick = Duration.ofNanos((long) (Math.floor((1d / rps) * 1e9)));
    Duration nextTickInMillis = nextTick.minus(timeAdvanced);
    LOG.debug("advancedMs{}, nextTickInMs={}, rps={}, durationSec={}, slope={}",
        timeAdvanced.toMillis(), nextTickInMillis.toMillis(), rps, durationSec, slope);
    if (nextTickInMillis.toMillis() < 0) {
      lastRequestTime = now;
      return Duration.ZERO;
    }
    lastRequestTime = now.plus(nextTickInMillis);
    return nextTickInMillis;
  }

  public <T> Mono<T> mono() {
    return (Mono<T>) Mono.delay(getTimeToWait());
  }
}
