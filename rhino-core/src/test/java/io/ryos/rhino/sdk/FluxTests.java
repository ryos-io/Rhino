package io.ryos.rhino.sdk;

import static io.ryos.rhino.sdk.runners.Throttler.throttle;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static reactor.core.publisher.Flux.range;
import static reactor.core.publisher.Mono.fromFuture;

import io.ryos.rhino.sdk.runners.Throttler.Limit;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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

  @Test
  void fluxing() {
    // flatmapping two fluxes -> create one publisher with interleaved emissions
    Flux.range(1, 3)
        // shit only one created, other ranged not emitted
        .zipWith(Mono.just("somedata")) // [1,somedata]
        .log()
        .zipWith(Mono.delay(Duration.ofSeconds(1)))
        .doOnEach(System.out::println)
        .blockLast();
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
  @Disabled("Long running verification tests.")
  void testThrottling() throws InterruptedException {
    final var latch = new CountDownLatch(1);
    final Limit limit = Limit.of(1, Duration.ofSeconds(5));
    final Limit limit2 = Limit.of(4, Duration.ofSeconds(5));
    Flux.range(0, 1000)
        .flatMap(d -> Mono.just(d * 2))
        .transform(throttle(limit, limit2))
        .take(ofSeconds(15))
        .doOnComplete(latch::countDown)
        .subscribe(n -> LOG.info("" + n));

    latch.await();
  }
}

