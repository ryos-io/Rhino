package io.ryos.rhino.sdk;

import static io.ryos.rhino.sdk.runners.Throttler.throttle;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofNanos;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static reactor.core.publisher.Flux.range;
import static reactor.core.publisher.Mono.fromFuture;

import io.ryos.rhino.sdk.runners.Throttler.Rps;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

class FluxTests {
  private static final Logger LOG = LoggerFactory.getLogger(FluxTests.class);

  private final CompletableFuture<String> erroneousFuture =
      CompletableFuture.<String>supplyAsync(() -> {
        throw new IllegalStateException("Tom caught Jerry");
      });

  // just demonstrates that an exception is thrown when calling `get`
  // which Reactor will do in the Flux pipeline eventually
  // question is, if the thrown exception is propagated downstream (see other tests)
  @Test
  void demonstrateExceptionInFuture() {
    Assertions
        .assertThrows(ExecutionException.class, this.erroneousFuture::get, "Tom caught Jerry");
  }

  @Test
  void stopImmediatelyAfterError() {
    Flux.just("alpha", "bravo")
        .concatWith(fromFuture(this.erroneousFuture))
        .concatWithValues("gamma")
        .log()
        .as(StepVerifier::create)
        .expectNext("alpha")
        .expectNext("bravo")
        .expectErrorMatches(e -> e instanceof CompletionException
            && e.getMessage().equals("java.lang.IllegalStateException: Tom caught Jerry"))
        .verify(ofSeconds(5));
  }

  @Test
  void demonstrateErrorHandling() {
    Flux.just("alpha", "bravo")
        .concatWith(fromFuture(this.erroneousFuture))
        .onErrorReturn("!ERROR!")
        .concatWithValues("gamma")
        .log()
        .as(StepVerifier::create)
        .expectNext("alpha")
        .expectNext("bravo")
        .expectNext("!ERROR!")
        .expectNext("gamma")
        .expectComplete()
        .verify(ofSeconds(5));
  }

  /*
   * I don't suggest use sampling as a throttling method for HTTP requests.
   * The problem is that it does not apply backpressure.
   * Instead it drops all but the last emitted element of the source Flux.
   */
  @Test
  void throttleWithSampling() throws InterruptedException {
    final var latch = new CountDownLatch(1);

    // 2 elements / s
    final var scheduler = VirtualTimeScheduler.getOrSet();
    final var virtualTimedFlux =
        Flux.interval(ofSeconds(0), ofMillis(100), scheduler);

    final int[] counter = {0};
    virtualTimedFlux
        .sample(ofMillis(500))
        .take(ofSeconds(3))
        .subscribe(n -> {
          LOG.info("" + n);
          counter[0]++;
        }, n -> latch.countDown(), latch::countDown);

    scheduler.advanceTimeBy(ofSeconds(1));
    assertTrue(counter[0] <= 2, "max 2 el/s expected");
    LOG.info("---");
    scheduler.advanceTimeBy(ofSeconds(1));
    assertTrue(counter[0] <= 4, "max 2 el/s expected");
    LOG.info("---");
    scheduler.advanceTimeBy(ofSeconds(1));
    assertTrue(counter[0] <= 6, "max 2 el/s expected");

    latch.await();
  }

  /**
   * In contrast to sampling delay applies backpressure to the upstream.
   * Thus, it can be used to limit the rps against a remote API.
   */
  @Test
  void throttleWithDelay() {
    final Supplier<Publisher<? extends Integer>> fluxSupplier = () ->
        range(0, 1000)
            .delayElements(ofMillis(500))
            .take(ofSeconds(3))
            .log();

    StepVerifier.withVirtualTime(fluxSupplier)
        .expectSubscription()
        .thenAwait(ofSeconds(1))
        .expectNextCount(2)
        .thenAwait(ofSeconds(1))
        .expectNextCount(2)
        .thenAwait(ofSeconds(1))
        .expectNextCount(1)
        .expectComplete()
        .verify();
  }

  /**
   * For 3 seconds generate 2 elements per second.
   * Generate 4 elements per second for 3 seconds, after
   */
  @Test
  void stairWiseRampump() {
    final Supplier<Publisher<? extends Integer>> fluxSupplier = () ->
        // 2 el/s for 3 seconds
        range(0, 1000)
            .delayElements(ofMillis(500))
            .take(ofSeconds(3))
            .concatWith(
                // 4 el/s for 3 seconds
                range(0, 1000)
                    .delayElements(ofMillis(250))
                    .take(ofSeconds(3)))
            .log();

    StepVerifier.withVirtualTime(fluxSupplier)
        .expectSubscription()
        .thenAwait(ofSeconds(1))
        .expectNextCount(2)
        .thenAwait(ofSeconds(1))
        .expectNextCount(2)
        .thenAwait(ofSeconds(1))
        .expectNextCount(1)
        .thenAwait(ofSeconds(1))
        .expectNextCount(4)
        .thenAwait(ofSeconds(1))
        .expectNextCount(4)
        .thenAwait(ofSeconds(1))
        .expectNextCount(3)
        .verifyComplete();

  }

  @Test
  void testThrottling() throws InterruptedException {
    // start with 2 el / s
    // add 10 el/s for 10 seconds
    // hold for 10 seconds
    // end
    final var latch = new CountDownLatch(1);
    Flux.<Long>generate(sink -> sink.next(System.currentTimeMillis()))
        .delayUntil(d -> {
          // 350rps => 0.350 request per ms =>
          var hit = System.currentTimeMillis();
          long seconds = (hit - d) / 1000;
          LOG.debug("delta {}", seconds);
          if (seconds <= 3) {
            return Mono.delay(ofMillis(500));
          } else {
            return Mono.delay(ofMillis(250));
          }
        })
        .take(ofSeconds(6))
        .doOnComplete(latch::countDown)
        .subscribe(n -> LOG.info("" + n));
    latch.await();
  }

  @Test
  void testThrottling2() throws InterruptedException {
    // start with 2 el / s
    // add 10 el/s for 10 seconds
    // hold for 10 seconds
    // end
    final var latch = new CountDownLatch(1);
    Flux.range(0, 1000)
        .parallel()
        .sequential()
        .zipWith(Flux.interval(ofSeconds(1)).take(ofSeconds(3)).concatWith(d -> Flux.just(0)))
        .take(ofSeconds(6))
        .doOnComplete(latch::countDown)
        .subscribe(n -> LOG.info("" + n));
    latch.await();
  }

  @Test
  void testThrottling3() throws InterruptedException {
    // start with 2 el / s
    // add 10 el/s for 10 seconds
    // hold for 10 seconds
    // end
    final var latch = new CountDownLatch(1);
    final Rps rps = Rps.of(1, 5);
    final Rps rps2 = Rps.of(4, 5);
    Flux.range(0, 1000)
        .transform(throttle(rps, rps2))
        .take(ofSeconds(15))
        .doOnComplete(latch::countDown)
        .subscribe(n -> LOG.info("" + n));

    // 1000000ns -> 1ms -> 0.001s
    // 200rps => tick each 1/200 s => 0.005ms => 50000ns
    // 133 rps => tick each 1/133s => 0,0075ms => 75000ns
    // throttle(flux, RequestPerSecond(rps, durationSec)))
    latch.await();
  }

  @Test
  void testRampump() throws InterruptedException {
    // start: 2el/s
    // end: 20el/s
    // duration: 20seconds
    // 2el/s + (20-2)/20 * t = 2el/s + 18/20 * t
    final var latch = new CountDownLatch(1);

    final long start = 1;
    final long end = 20;
    final long duration = 10;
    final double slope = ((double) (end - start)) / duration;

    final long startTime = System.currentTimeMillis();
    Flux.range(0, 1000)
        .zipWith(
            Flux.<Long>generate(sink -> sink.next(0L)).delayUntil(d -> {
              long t = (System.currentTimeMillis() - startTime) / 1000;
              long rps = (long) Math.floor(start + slope * t);
              long tickNano = (long) Math.floor((1d / rps) * 1e9);
              LOG.debug("tickNano={}", tickNano);
              return Mono.delay(ofNanos(tickNano));
            }))
        .take(ofSeconds(duration))
        .doOnComplete(latch::countDown)
        .subscribe(n -> LOG.info("" + n));
    latch.await();
  }
}

