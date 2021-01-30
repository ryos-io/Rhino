package io.ryos.rhino.sdk.runners;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.RequestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RampupTest {
  private static WireMockServer SERVER;
  private static AsyncHttpClient ASYNC_CLIENT;

  @BeforeAll
  static void beforeAll() {
    SERVER = new WireMockServer(wireMockConfig().port(8088)
        .jettyAcceptors(2)
        .jettyAcceptQueueSize(100)
        .containerThreads(100));
    SERVER.start();
    WireMock.configureFor("localhost", SERVER.port());
    ASYNC_CLIENT = Dsl.asyncHttpClient();
  }

  @BeforeEach
  void beforeEach() {
    WireMock.stubFor(WireMock.get(urlEqualTo("/"))
        .willReturn(aResponse()
            .withStatus(200)));
  }

  @Test
  void rampup() {
    Duration duration = Duration.ofSeconds(10);
    var rampup = new Rampup(1, 10, duration);
    Supplier<Flux<Map.Entry<Integer, Integer>>>
        flux = () -> Flux.fromStream(IntStream.range(1, 56).boxed())
        .take(duration)
        .flatMap(i ->
            // ruft onNext auf flatMap auf
            rampup.rampUp(i).flatMap(v -> Mono.fromCompletionStage(
                ASYNC_CLIENT.executeRequest(
                    new RequestBuilder()
                        .setMethod("GET")
                        .setUrl(SERVER.baseUrl())
                        .build())
                    .toCompletableFuture()
                    .thenApply(response -> Map.entry(i, response.getStatusCode())))))
        .log();
    flux.get().subscribe();
    StepVerifier.setDefaultTimeout(Duration.ofSeconds(3));
    StepVerifier.withVirtualTime(flux)
        .expectSubscription()
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(1)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(1)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(2)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(3)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(4)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(5)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(6)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(7)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(8)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(9)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(10)
        .thenCancel();
  }

  @Test
  void constRps() {
    Duration duration = Duration.ofSeconds(10);
    var rampup = new Rampup(1, 1, duration);
    Supplier<Flux<Integer>>
        flux = () -> Flux.fromStream(IntStream.range(1, 100).boxed())
        .flatMap(rampup::rampUp)
        .flatMap(i ->
            // ruft onNext auf flatMap auf
            Mono.fromCompletionStage(
                ASYNC_CLIENT.executeRequest(
                    new RequestBuilder()
                        .setMethod("GET")
                        .setUrl(SERVER.baseUrl())
                        .build())
                    .toCompletableFuture()
                    .thenApply(response -> Map.entry(i, response.getStatusCode()))))
        .map(Map.Entry::getKey)
        .take(duration);
    StepVerifier.setDefaultTimeout(Duration.ofSeconds(3));
    StepVerifier.withVirtualTime(flux)
        .expectSubscription()
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(1)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(1)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(1)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(1)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(1)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(1)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(1)
        .thenAwait(Duration.of(1, SECONDS))
        .expectNextCount(1)
        .thenAwait(Duration.of(1, SECONDS))
        .thenCancel();
  }
}