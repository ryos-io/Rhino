package io.ryos.rhino.sdk;

import java.time.Duration;

public class ThrottlingInfo {

  private int numberOfRequests;
  private Duration duration;

  public ThrottlingInfo(final int numberOfRequests, final Duration duration) {
    this.numberOfRequests = numberOfRequests;
    this.duration = duration;
  }

  public int getNumberOfRequests() {
    return numberOfRequests;
  }

  public Duration getDuration() {
    return duration;
  }
}
