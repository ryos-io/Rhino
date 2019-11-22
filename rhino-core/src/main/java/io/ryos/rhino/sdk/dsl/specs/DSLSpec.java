package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.dsl.specs.impl.HttpSpecImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.SomeSpecImpl;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;

/**
 * Load testing specification for reactive runner.
 *
 * @author Erhan Bagdemir
 * @see ReactiveHttpSimulationRunner
 * @since 1.1.0
 */
public interface DSLSpec extends DSLItem, Materializable {

  /**
   * Static factory method to create a new {@link HttpSpec} instance.
   *
   * @param measurementPoint Measurement point name.
   * @return A new instance of {@link DSLSpec}.
   */
  static HttpConfigSpec http(String measurementPoint) {
    return new HttpSpecImpl(measurementPoint);
  }

  static SomeSpec some(String measurementPoint) {
    return new SomeSpecImpl(measurementPoint);
  }
}
