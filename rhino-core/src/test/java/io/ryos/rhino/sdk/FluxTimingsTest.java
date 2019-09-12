/*
 * Copyright 2018 Ryos.io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ryos.rhino.sdk;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.filter.ThrottleRequestFilter;
import org.junit.Rule;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FluxTimingsTest {

  public static final String TARGET = "http://localhost:8089/api/files";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().port(8089)
      .jettyAcceptors(2)
      .jettyAcceptQueueSize(100)
      .containerThreads(100));

  @Test
  public void testFluxTiming() {

    var httpClientConfig = Dsl.config()
        .setKeepAlive(true)
        .setMaxConnections(1)
        .setConnectTimeout(60000)
        .setHandshakeTimeout(60000)
        .setReadTimeout(4000)
        .addRequestFilter(new ThrottleRequestFilter(1))
        .build();

    var client = Dsl.asyncHttpClient(httpClientConfig);

    stubFor(WireMock.get(urlEqualTo("/api/files"))
        .willReturn(aResponse()
            .withFixedDelay(2000)
            .withStatus(200)));

    System.out.println("Before flux");

    Flux.fromArray(new Integer [] { 1, 2, 3 })
        .flatMap(i ->
            Mono.fromCompletionStage(client.executeRequest(Dsl.get(TARGET).build()).toCompletableFuture())
                .doOnNext(r -> System.out.println(r.getStatusCode())))
        .doOnComplete(() -> {
          System.out.println("completed");
        })
        .blockLast();
  }
}
