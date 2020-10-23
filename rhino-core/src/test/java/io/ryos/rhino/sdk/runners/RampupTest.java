package io.ryos.rhino.sdk.runners;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RampupTest {
  private static final Logger LOG = LoggerFactory.getLogger(RampupTest.class);

  private static class VirtualClock {
    Instant start = Instant.now();
    Instant current = start;

    public VirtualClock() {
    }

    public VirtualClock(final Instant start) {
      this.start = start;
    }

    public Instant getStartTime() {
      return start;
    }

    public Instant advance(Duration duration) {
      current = current.plus(duration);
      return current;
    }

    public Instant getCurrentTime() {
      return current;
    }

    public Duration getAdvancedTime() {
      return Duration.of(start.until(current, MILLIS), MILLIS);
    }
  }

  @Test
  void testConcurrentRequestsAtTheSameTime() throws InterruptedException {
    Instant now = Instant.now();
    VirtualClock clock = new VirtualClock(now);
    VirtualClock helperClock = new VirtualClock(now);
    int startRps = 1;
    int targetRps = 30;
    Duration duration = Duration.of(30, ChronoUnit.SECONDS);
    Rampup rampup = new Rampup(clock.getStartTime(),
        startRps, targetRps, duration);

    final Duration[] previousTick = new Duration[] {Duration.ZERO};
    Supplier<Duration> expectedTick = () -> {
      Duration tick = getTick(helperClock, startRps, targetRps, duration);
      helperClock.advance(tick);
      previousTick[0] = previousTick[0].plus(tick);
      return previousTick[0];
    };

    while (helperClock.getAdvancedTime().minus(duration).isNegative()) {
      assertThat(rampup.getTimeToWait(clock::getCurrentTime), is(expectedTick.get()));
    }
  }

  @Test
  void testConcurrentRequestsAtDifferentTimes() {
    Instant now = Instant.now();
    VirtualClock clock = new VirtualClock(now);
    int startRps = 1;
    int targetRps = 10;

    Duration duration = Duration.of(10, ChronoUnit.SECONDS);
    Rampup rampup = new Rampup(clock.getStartTime(),
        startRps, targetRps, duration);

    assertThat(rampup.getTimeToWait(clock::getCurrentTime), is(Duration.of(1000, MILLIS)));
    // between the tick generation some delay happened (2s) - the time has to be corrected and the caller should go on immediately (0 delay)
    clock.advance(Duration.of(2000, MILLIS));
    assertThat(rampup.getTimeToWait(clock::getCurrentTime), is(Duration.of(0, MILLIS)));
    // however if the next requester asks at the same time, it has to wait
    assertThat("Tick should be greater than 0",
        rampup.getTimeToWait(clock::getCurrentTime).toMillis() > 0);
  }

  @Test
  void testWithinFlux() {
    Instant now = Instant.now();
    Rampup instance = new Rampup(now, 1, 10, Duration.of(10, SECONDS));
    Flux<Integer> flux = Flux.range(1, 100)
        .repeat(1)
        .flatMap(n -> Mono.just(n).delayElement(instance.getTimeToWait()))
        .log();
    StepVerifier.setDefaultTimeout(Duration.of(1, SECONDS));
    StepVerifier.withVirtualTime(() -> flux)
        .expectSubscription()
        .thenAwait(Duration.of(1, SECONDS))
        .expectNext(1)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNext(2, 3)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNext(4, 5, 6)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNext(7, 8, 9, 10)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNext(11, 12, 13, 14, 15)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNext(16, 17, 18, 19, 20, 21)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNext(22, 23, 24, 25, 26, 27, 28)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNext(29, 30, 31, 32, 33, 34, 35, 36)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNext(37, 38, 39, 40, 41, 42, 43, 44, 45)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNext(46, 47, 48, 49, 50, 51, 52, 53, 54, 55)
        .thenAwait(Duration.of(20, SECONDS))
        .expectNextCount(45 + 100) // get the rest
        .expectComplete()
        .verify();
  }

  @Test
  void testConstantThrottling() {
    Instant now = Instant.now();
    Rampup instance = new Rampup(now, 2, 2, Duration.of(6, SECONDS));
    Flux<Integer> flux = Flux.range(1, 6)
        .flatMap(n -> Mono.just(n).delayElement(instance.getTimeToWait()))
        .log();
    StepVerifier.setDefaultTimeout(Duration.of(1, SECONDS));
    StepVerifier.withVirtualTime(() -> flux)
        .expectSubscription()
        .expectNoEvent(Duration.of(300, MILLIS))
        .thenAwait(Duration.of(200, MILLIS))
        .expectNext(1)
        .thenAwait(Duration.of(500, MILLIS))
        .expectNext(2)
        .thenAwait(Duration.of(500, MILLIS))
        .expectNext(3)
        .thenAwait(Duration.of(500, MILLIS))
        .expectNext(4)
        .thenAwait(Duration.of(500, MILLIS))
        .expectNext(5)
        .thenAwait(Duration.of(500, MILLIS))
        .expectNext(6)
        .expectComplete()
        .verify();
  }

  double getRps(VirtualClock clock, double startRps, double targetRps, Duration duration) {
    double slope = (targetRps - startRps) / duration.toSeconds();
    var rps = startRps + (slope * (clock.getAdvancedTime().toMillis() / 1e3D));
    return rps;
  }

  Duration getTick(VirtualClock clock, double startRps, double targetRps, Duration duration) {
    var rps = getRps(clock, startRps, targetRps, duration);
    var tick = (1 / rps) * 1e3D;
    return Duration.of((long) tick, MILLIS);
  }
}