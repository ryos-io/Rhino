package io.ryos.rhino.sdk;

import java.time.Duration;

public class RampupInfo {

  private long startRps;
  private long targetRps;
  private Duration duration;

  public RampupInfo(final long startRps, final long targetRps, final Duration duration) {
    this.startRps = startRps;
    this.targetRps = targetRps;
    this.duration = duration;
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
