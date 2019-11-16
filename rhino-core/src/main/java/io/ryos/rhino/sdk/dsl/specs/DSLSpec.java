package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.mat.SpecMaterializer;
import io.ryos.rhino.sdk.dsl.specs.impl.HttpSpecImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.SomeSpecImpl;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;

/**
 * Load testing specification for reactive runner.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see ReactiveHttpSimulationRunner
 * @since 1.1.0
 */
public interface DSLSpec extends DSLItem {

  /**
   * Static factory method to create a new {@link HttpSpec} instance.
   * <p>
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

  /**
   * Create materializer instance for this spec instance.
   * <p>
   *
   * @param userSession User session of current load cycle.
   * @param <T> Result object type.
   * @return {@link SpecMaterializer} instance.
   */
  <T extends DSLSpec> SpecMaterializer<T> createMaterializer(UserSession userSession);
}
