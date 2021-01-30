package io.ryos.rhino.sdk;

import java.time.Duration;

public class RampupInfo {
  private long startRps;
  private long targetRps;
  private Duration duration;
  private static final RampupInfo NONE = new RampupInfo(-1, -1, Duration.ZERO);

  private RampupInfo(final long startRps, final long targetRps, final Duration duration) {
    this.startRps = startRps;
    this.targetRps = targetRps;
    this.duration = duration;
  }

  public static RampupInfo ofDefault(final long startRps, final long targetRps,
      final Duration duration) {
    return new RampupInfo(startRps, targetRps, duration);
  }

  public static RampupInfo none() {
    return NONE;
  }

  public long getStartRps() {
    return startRps;
  }

  public long getTargetRps() {
    return targetRps;
  }

  public Duration getDuration() {
    return duration;
  }
}
