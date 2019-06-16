/*************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2019 Adobe
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import org.asynchttpclient.AsyncCompletionHandlerBase;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

public class Fluxample {
  private static final Logger LOG = LoggerFactory.getLogger(Fluxample.class);
  private static final String HEALTH_ENDPOINT = "http://localhost:3000";

  void run() throws InterruptedException {
    final var latch = new CountDownLatch(1);
    Flux.generate((sink) -> sink.next(Math.random()))
        .log()
        .take(1000)
        .parallel()
        .runOn(Schedulers.elastic())
        .doOnComplete(latch::countDown)
        .subscribe(e -> LOG.info("Random number: {}", e));

    latch.await();
  }

  void run2() throws InterruptedException, IOException {
    final var httpClientConfig = Dsl.config()
        .setConnectTimeout(10)
        .setMaxConnections(1000)
        .setKeepAlive(true)
        .build();

    final var client = Dsl.asyncHttpClient(httpClientConfig);

    final var latch = new CountDownLatch(1);
    Flux.range(0, 10)
        .flatMap(i -> Mono.subscriberContext().map(ctx -> {
          Map<String, Object> session = getSession(ctx);
          session.put("RequestNo", i);
          return ctx;
        }))
        .flatMap(session -> Mono.fromFuture(client.executeRequest(
            Dsl.request("GET", HEALTH_ENDPOINT),
            new AsyncCompletionHandlerBase() {
              @Override
              public Response onCompleted(final Response response) throws Exception {
                LOG.info("Got response {}", response.getStatusCode());
                return super.onCompleted(response);
              }

              @Override
              public void onThrowable(final Throwable t) {
                throw new RuntimeException(t);
              }
            }
        ).toCompletableFuture()))
        .log()
        .doOnComplete(() -> {
          LOG.info("Completed.");
          latch.countDown();
        })
        .doOnCancel(() -> {
          LOG.info("Canceled.");
          latch.countDown();
        })
        .subscriberContext(ctx -> ctx.put("session", new HashMap<>()))
        .parallel()
        .runOn(Schedulers.elastic())
        .subscribe(response -> LOG.info("Got response {}", response.getStatusCode()),
            t -> LOG.error("Got Error: {}", t.getMessage()));

    latch.await();
    client.close();
  }

  void run3() throws InterruptedException, IOException {
    final var httpClientConfig = Dsl.config()
        .setConnectTimeout(10)
        .setMaxConnections(1000)
        .setKeepAlive(true)
        .build();

    final var client = Dsl.asyncHttpClient(httpClientConfig);

    final var latch = new CountDownLatch(1);
    Flux.range(0, 10)
        .flatMap(i -> Mono.subscriberContext().map(ctx -> {
          Map<String, Object> session = getSession(ctx);
          session.put("RequestNo", i);
          return ctx;
        }))
        .flatMap(session -> Mono.fromFuture(client.executeRequest(
            Dsl.request("GET", HEALTH_ENDPOINT),
            new AsyncCompletionHandlerBase() {
              @Override
              public Response onCompleted(final Response response) throws Exception {
                LOG.info("Got response {}", response.getStatusCode());
                return super.onCompleted(response);
              }

              @Override
              public void onThrowable(final Throwable t) {
                throw new RuntimeException(t);
              }
            }
        ).toCompletableFuture()))
        .flatMap(response -> Mono.subscriberContext().map(ctx -> {
          Map<String, Object> session = getSession(ctx);
          session.put("successful", true);
          return ctx;
        }))
        .log()
        .doOnComplete(() -> {
          LOG.info("Completed.");
          latch.countDown();
        })
        .doOnCancel(() -> {
          LOG.info("Canceled.");
          latch.countDown();
        })
        .subscriberContext(ctx -> ctx.put("session", new HashMap<>()))
        .parallel()
        .runOn(Schedulers.elastic())
        .subscribe(ctx -> LOG.info("Got response {}", getSession(ctx)),
            t -> LOG.error("Got Error: {}", t.getMessage()));
    //                .subscribe(response -> LOG.info("Got response {}", response.getStatusCode()), t -> LOG.error("Got Error: {}", t.getMessage()));

    latch.await();
    client.close();
  }

  void testClient() throws InterruptedException, ExecutionException, IOException {
    final var httpClientConfig = Dsl.config()
        .setConnectTimeout(10)
        .setMaxConnections(1000)
        .setKeepAlive(true)
        .build();

    final var client = Dsl.asyncHttpClient(httpClientConfig);

    final CompletableFuture<Response> future = client.executeRequest(
        Dsl.request("GET", HEALTH_ENDPOINT),
        new AsyncCompletionHandlerBase() {
          @Override
          public Response onCompleted(final Response response) throws Exception {
            LOG.info("Got response {}", response.getStatusCode());
            return super.onCompleted(response);
          }

          @Override
          public void onThrowable(final Throwable t) {
            LOG.error("error", t);
          }
        }
    ).toCompletableFuture();

    final Response response = future.get();
    client.close(); // have to do otherwise it hangs
  }

  private Map<String, Object> getSession(final Context ctx) {
    return ctx.get("session");
  }

  public static void main(final String[] args) throws Exception {
    //    new Fluxample().run();
    new Fluxample().run2();
    //      new Fluxample().testClient();
    LOG.info("Bye world");
  }

}
