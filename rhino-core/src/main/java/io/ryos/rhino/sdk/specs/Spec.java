package io.ryos.rhino.sdk.specs;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;

/**
 * Load testing specification for reactive runner.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see io.ryos.rhino.sdk.runners.ReactiveSimulationRunner
 * @since 1.2.0
 */
public interface Spec {

  static HttpSpec http() {
    return new HttpSpecImpl();
  }

  //TODO This method and underlying reactive framework is to be abstracted away.
  Mono<HttpClientResponse> toMono();
}