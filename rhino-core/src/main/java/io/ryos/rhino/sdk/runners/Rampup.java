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
  private static Rampup INSTANCE;

  private static class VirtualTime implements Comparable<VirtualTime> {
    private Instant start;
    private Instant lastTime;

    public VirtualTime(final Instant start) {
      this.start = start;
      this.lastTime = start;
    }

    public void advance(Duration duration) {
      lastTime = lastTime.plus(duration);
    }

    public void until(Instant time) {
      lastTime = lastTime.plus(time.toEpochMilli(), ChronoUnit.MILLIS);
    }

    public static VirtualTime from(VirtualTime virtualTime) {
      VirtualTime o = new VirtualTime(virtualTime.start);
      o.until(o.lastTime);
      return o;
    }

    public Duration delta() {
      return Duration.between(start, lastTime);
    }

    @Override
    public int compareTo(final VirtualTime o) {
      return this.delta().compareTo(o.delta());
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(Rampup.class);

  private VirtualTime virtualTime;
  private final long startRps;

  private final double slope;
  private long actualRps = 0;
  private long delayMultiplicator = 0;
  private final long targetRps;

  public static Rampup getInstance() {
    if (INSTANCE == null) {
      RampupInfo rampupInfo = SimulationConfig.getRampupInfo();
      if (rampupInfo == RampupInfo.none()) {
        return null;
      }
      if (rampupInfo.getStartRps() < 0) {
        throw new IllegalArgumentException("StartRps must be greater than 0");
      }
      if (rampupInfo.getTargetRps() < rampupInfo.getStartRps()) {
        throw new IllegalArgumentException("TargetRps must be greater than startRps");
      }
      INSTANCE = new Rampup(rampupInfo.getStartRps(), rampupInfo.getTargetRps(),
          rampupInfo.getDuration());
      return INSTANCE;
    }
    return INSTANCE;
  }

  public Rampup(final long startRps, final long targetRps,
      final Duration duration) {
    this.startRps = startRps;
    this.targetRps = targetRps;
    if (startRps == targetRps) {
      slope = 0;
    } else {
      slope = ((double) (targetRps - startRps)) / duration.toSeconds();
    }
  }

  public VirtualTime getVirtualTime() {
    if (virtualTime == null) {
      virtualTime = new VirtualTime(Instant.now());
    }
    return virtualTime;
  }

  public <T> Mono<T> rampUp(T value) {
    var res = Mono.just(value);
    actualRps++;
    var rps = startRps;
    if (slope > 0) {
      rps = Math.min((long) ((slope * (getVirtualTime().delta().toMillis() / 1000d)) + startRps),
          targetRps);
    }
    LOG.debug("actualRps={}, targetRps={}, advancedTimeS={}, delayMultiplicator={}",
        actualRps, rps, getVirtualTime().delta().toSeconds(), delayMultiplicator);

    // delay everything, otherwise i can't use flatmap
    Duration interval = Duration.ofSeconds(1);
    Duration delay = interval.multipliedBy(delayMultiplicator);
    if (actualRps >= rps) {
      delayMultiplicator++;
      getVirtualTime().advance(interval);
      res = res.delayElement(delay);
      actualRps = 0;
    }

    //    LOG.debug("delayMs={}", delay.toMillis());
    return res.delayElement(delay);
  }
}
