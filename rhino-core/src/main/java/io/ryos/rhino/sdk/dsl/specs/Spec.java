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
public interface Spec {

  enum Scope {
    USER,
    SIMULATION
  }

  /**
   * Static factory method to create a new {@link HttpSpec} instance.
   * <p>
   *
   * @param measurementPoint Measurement point name.
   * @return A new instance of {@link Spec}.
   */
  static HttpConfigSpec http(String measurementPoint) {
    return new HttpSpecImpl(measurementPoint);
  }

  static SomeSpec some(String measurementPoint) {
    return new SomeSpecImpl(measurementPoint);
  }

  Scope getSessionScope();

  void setSessionScope(Scope scope);

  /**
   * Whether the measurement is enabled.
   * <p>
   *
   * @return True if measurement is enabled.
   */
  boolean isMeasurementEnabled();

  /**
   * Whether the measurement is cumulative.
   * <p>
   *
   * @return True if cumulative measurement is enabled.
   */
  boolean isCumulativeMeasurement();

  /**
   * The name of the spec. It is the step name in scenario counterpart.
   * <p>
   *
   * @return The name of the spec.
   */
  String getMeasurementPoint();

  /**
   * Returns the test/DSL name.
   * <p>
   *
   * @return Test or DSL name.
   */
  String getTestName();

  /**
   * Setter for test or DSL name.
   * <p>
   *
   * @param testName The test name from annotation.
   */
  void setTestName(String testName);

  /**
   * Create materializer instance for this spec instance.
   * <p>
   *
   * @param userSession User session of current load cycle.
   * @param <T> Result object type.
   * @return {@link SpecMaterializer} instance.
   */
  <T extends Spec> SpecMaterializer<T> createMaterializer(UserSession userSession);
}
