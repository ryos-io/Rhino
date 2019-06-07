package io.ryos.rhino.sdk.specs;

import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;

/**
 * Load testing specification for reactive runner.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see ReactiveHttpSimulationRunner
 * @since 1.2.0
 */
public interface Spec {

  static HttpSpec http(String measurementPoint) {
    return new HttpSpecImpl(measurementPoint);
  }

  Spec withSpecName(String name);

  /**
   * The name of the spec. It is the step name in scenario countpart.
   * <p>
   *
   * @return The name of the spec.
   */
  String getName();
}
