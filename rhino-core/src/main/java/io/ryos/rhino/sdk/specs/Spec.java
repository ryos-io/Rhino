package io.ryos.rhino.sdk.specs;

import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;

/**
 * Load testing specification for reactive runner.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see ReactiveHttpSimulationRunner
 * @since 1.2.0
 */
public interface Spec {

  static HttpSpec http(String stepName) {
    return new HttpSpecImpl(stepName);
  }

  //TODO This method and underlying reactive framework is to be abstracted away.
  Mono<HttpClientResponse> toMono();

  Spec withSpecName(String name);
}