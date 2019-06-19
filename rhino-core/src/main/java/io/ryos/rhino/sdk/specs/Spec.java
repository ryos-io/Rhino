package io.ryos.rhino.sdk.specs;

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

  /**
   * Static factory method to create a new {@link HttpSpec} instance.
   * <p>
   *
   * @param measurementPoint Measurement point name.
   * @return A new instance of {@link Spec}.
   */
  static HttpSpec http(String measurementPoint) {
    return new HttpSpecImpl(measurementPoint);
  }

  static SomeSpecImpl some(String measurementPoint) {
    return new SomeSpecImpl(measurementPoint);
  }

  /**
   * The name of the spec. It is the step name in scenario countpart.
   * <p>
   *
   * @return The name of the spec.
   */
  String getMeasurementPoint();

  String getTestName();

  void setTestName(String testName);
}
