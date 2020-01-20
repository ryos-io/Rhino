package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.dsl.impl.HttpDslImpl;
import io.ryos.rhino.sdk.dsl.impl.SomeDslImpl;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;

/**
 * Load testing specification for reactive runner.
 *
 * @author Erhan Bagdemir
 * @see ReactiveHttpSimulationRunner
 * @since 1.1.0
 */
public interface MaterializableDslItem extends DslItem, MaterializableDsl {

  /**
   * Static factory method to create a new {@link HttpDsl} instance.
   *
   * @param name Measurement point name.
   * @return A new instance of {@link MaterializableDslItem}.
   */
  static HttpConfigDsl http(String name) {
    return new HttpDslImpl(name);
  }

  static SomeDsl some(String name) {
    return new SomeDslImpl(name);
  }
}
