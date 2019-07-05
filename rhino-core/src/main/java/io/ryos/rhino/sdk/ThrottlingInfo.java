package io.ryos.rhino.sdk;

import java.time.Duration;

public class ThrottlingInfo {

  private int rps;
  private Duration duration;

  public ThrottlingInfo(final int rps, final Duration duration) {
    this.rps = rps;
    this.duration = duration;
  }

  public int getRps() {
    return rps;
  }

  public Duration getDuration() {
    return duration;
  }
}
