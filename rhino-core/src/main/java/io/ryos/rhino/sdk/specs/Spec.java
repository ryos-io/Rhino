package io.ryos.rhino.sdk.specs;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;

/**
 * @author Erhan Bagdemir
 */
public interface Spec {

  static HttpSpec http() {
    return new HttpSpecImpl();
  }

  Mono<HttpClientResponse> toMono();
}