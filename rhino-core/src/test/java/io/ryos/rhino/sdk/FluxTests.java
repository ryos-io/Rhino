package io.ryos.rhino.sdk;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static reactor.core.publisher.Flux.range;
import static reactor.core.publisher.Mono.fromFuture;

import io.ryos.rhino.sdk.runners.Rampup;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

@Disabled
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

  @Test
  void bla() {
    Mono.just("hello").take(Duration.ofSeconds(5))
        .flatMap(v -> Mono.just(v).delayElement(Duration.ofSeconds(10)))
        .log()
        .block();
  }

  @Test
  void testDelayElements() {
    Flux.fromStream(
        IntStream.range(0, 10).mapToObj(i -> "request" + i))
        .log()
        .delayElements(Duration.ofSeconds(1)) // requests 32 elements from stream to delay each
        .log()
        .blockLast(); // requests everything (unbound)
  }

  @Test
  void testFlatmapAndDelayElement() {
    AtomicInteger counter = new AtomicInteger();
    Flux.fromStream(
        IntStream.range(0, 10).mapToObj(i -> "request" + i))
        .log()
        // jokes on you if you expected that each elements comes with an increasing delay
        // since the next operator requests multiple elements at once
        // delay is constant since the nth element waits n seconds until it comes - 1el/s
        // delays are executed on a shared thread pool
        // each thread calls onNext when it is time
        .flatMap(str -> {
          System.out.println("waiting " + counter.get());
          return Mono.just(str).delayElement(ofSeconds(counter.getAndAdd(1)));
        })
        .log()
        .blockLast(); // requests everything (unbound)
  }

  @Test
  void testDelayUntil() {
    AtomicInteger counter = new AtomicInteger();
    Flux.fromStream(
        IntStream.range(0, 10).mapToObj(i -> "request" + i))
        .log()
        .delayUntil(str ->
            // delayed Mono is executed on a thread from a thread pool
            // but delays are scheduled one after another because delayUntil blocks execution
            Mono.just(str).delayElement(Duration.ofSeconds(counter.getAndAdd(1))))
        .log()
        .blockLast(); // requests everything (unbound)
  }

  @Test
  void rampupWithDelayElement() {
    AtomicInteger counter = new AtomicInteger(10);
    Flux.fromStream(IntStream.range(0, 10).mapToObj(i -> "request" + i))
        .log()
        .delayUntil(str ->
            // delayed Mono is executed on a thread from a thread pool
            // but delays are scheduled one after another because delayUntil blocks execution
            Mono.just(str).delayElement(Duration.ofMillis(100L * counter.getAndDecrement())))
        // by default buffers 256 elements before it starts mapping
        //        .log()
        .flatMap(str ->
            // ruft onNext auf flatMap auf
            Mono.fromCompletionStage(
                Dsl.asyncHttpClient().executeRequest(
                    new RequestBuilder()
                        .setMethod("GET")
                        .setUrl("https://google.de")
                        .build())
                    .toCompletableFuture())) // a short delay is expected since flatmap waits for some elements to process in parallel
        .map(Response::getStatusCode)
        .log()
        .blockLast();
  }

  @Test
  void rampup2() {
    AsyncHttpClient asyncHttpClient = Dsl.asyncHttpClient();
    Duration duration = ofSeconds(60);
    var rampup = new Rampup(1, 60, duration);
    AtomicInteger counter = new AtomicInteger();
    // this is broken - delay is completely ignored
    Flux.fromStream(IntStream.range(0, 100000).boxed())
        .flatMap(str -> {
          return rampup.rampUp(str).flatMap(v -> {
            LOG.info("time({}): {}", str, Instant.now());
            counter.incrementAndGet();
            return Mono.fromCompletionStage(
                asyncHttpClient.executeRequest(
                    new RequestBuilder()
                        .setMethod("GET")
                        .setUrl("https://google.de")
                        .build())
                    .toCompletableFuture()
                    .thenApply(response -> Map.entry(v, response.getStatusCode()))
                    .thenApply(res -> {
                      //                      System.out.println("time:" + Instant.now().toString());
                      counter.decrementAndGet();
                      return res;
                    }));
          });
        })
        //        .log()
        .take(duration)
        .blockLast();
  }

  @Test
  public void test() {
    Flux.just("a", "b", "c").log().blockLast();
    //[INFO] 2021-01-31 17:12:59,105 [main] [] reactor.Flux.Array.1 info - | onSubscribe([Synchronous Fuseable] FluxArray.ArraySubscription)
    //[INFO] 2021-01-31 17:12:59,112 [main] [] reactor.Flux.Array.1 info - | request(unbounded)
    //[INFO] 2021-01-31 17:12:59,114 [main] [] reactor.Flux.Array.1 info - | onNext(a)
    //[INFO] 2021-01-31 17:12:59,117 [main] [] reactor.Flux.Array.1 info - | onNext(b)
    //[INFO] 2021-01-31 17:12:59,119 [main] [] reactor.Flux.Array.1 info - | onNext(c)
    //[INFO] 2021-01-31 17:12:59,122 [main] [] reactor.Flux.Array.1 info - | onComplete()
  }

  @Test
  public void test2() {
    final Flux<String> flux = Flux.just("a", "b", "c");
    flux.delayElements(Duration.ofSeconds(1)).log().blockLast();
    //[INFO] 2021-01-31 17:15:59,061 [main] [] reactor.Flux.ConcatMap.1 info - onSubscribe(FluxConcatMap.ConcatMapImmediate)
    //        [INFO] 2021-01-31 17:15:59,067 [main] [] reactor.Flux.ConcatMap.1 info - request(unbounded)
    //        [INFO] 2021-01-31 17:16:00,114 [parallel-1] [] reactor.Flux.ConcatMap.1 info - onNext(a)
    //        [INFO] 2021-01-31 17:16:01,115 [parallel-2] [] reactor.Flux.ConcatMap.1 info - onNext(b)
    //        [INFO] 2021-01-31 17:16:02,116 [parallel-3] [] reactor.Flux.ConcatMap.1 info - onNext(c)
    //        [INFO] 2021-01-31 17:16:02,117 [parallel-3] [] reactor.Flux.ConcatMap.1 info - onComplete()

  }

  @Test
  public void test3() {
    final Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5);
    // delay wirkt sich auf alle auf einmal aus
    flux.flatMap(v -> {
      if (v % 2 == 0) {
        return Mono.just(v); // these will be go through everything
      }
      return Mono.just(v).delayElement(Duration.ofSeconds(v * 2));
    })
        .flatMap(v -> Mono.fromCompletionStage(
            CompletableFuture.supplyAsync(() -> {
              System.out.println("start " + v + " " + Instant.now());
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              System.out.println("end " + v + " " + Instant.now());
              return v + "fuck";
            }))).log()
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

  @Test
  void TestFutures() {
    Instant start = Instant.now();
    CompletableFuture<String> future = supplyAsync(() -> {
      while (Instant.now().isBefore(start.plus(5, ChronoUnit.SECONDS))) {
        System.out.println("fuck uuuu");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      return "HELLO";
    }, Executors.newSingleThreadExecutor());

    fromFuture(future)
        .doOnError(e -> System.out.println("GOT AN ERROR"))
        .doFinally(signalType -> {
          if (signalType == SignalType.CANCEL) {
            future.cancel(false);
          }
        })
        .map(val -> val.toLowerCase())
        .log()
        .as(StepVerifier::create)
        //        .expectComplete()
        .thenCancel()
        .verify();
    assertThat("future is not cancelled", future.isCancelled());
    try {
      future.join();
    } catch (CancellationException e) {
      System.out.println("Was cancelled");
    }
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
  void testThreads() {
    ExecutorService executorService = Executors.newFixedThreadPool(3);
    ExecutorService single = Executors.newSingleThreadExecutor();
    Flux.range(0, 100)
        .publishOn(Schedulers.single())
        .map(n -> n + "fo")
        .log()
        .flatMap(
            n -> Mono.fromFuture(supplyAsync(() -> {
              try {
                Thread.sleep(1000);
                return n + "fu";
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              return "failed";
            }, executorService)))
        .log()
        .blockLast();

  }

  @Test
  void testThreads2() {
    ExecutorService executorService = Executors.newFixedThreadPool(3);
    ExecutorService single = Executors.newSingleThreadExecutor();
    range(0, 1000)
        .flatMap(n ->
            Mono.just(n)
                .zipWith(
                    Mono.defer(() -> {
                      LOG.info("SAD STORY");
                      return Mono.just(100);
                    })
                )
                .subscribeOn(Schedulers.single())
                .log()
                .flatMap(
                    pair -> Mono.just(pair.getT1()).delayElement(ofMillis(pair.getT2())))
                .flatMap(d -> fromFuture(supplyAsync(() -> {
                  try {
                    Thread.sleep(1000);
                    return d + "fu";
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                  }
                  return "failed";
                }, executorService)))
                .map(d -> d + "fi")
        )
        .log()
        .blockLast();

  }
}

