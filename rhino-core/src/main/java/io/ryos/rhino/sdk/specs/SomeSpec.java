package io.ryos.rhino.sdk.specs;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.reporting.Measurement;
import java.util.function.BiFunction;

/**
 * Some spec is a custom spec to enable developers to add arbitrary code snippets into the DSL.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface SomeSpec extends Spec {

  /**
   * Function contains the code snippet to be applied.
   * <p>
   *
   * @return Spec function.
   */
  BiFunction<UserSession, Measurement, UserSession> getFunction();

  /**
   * Method to add a spec function into the DSL.
   * <p>
   */
  Spec as(BiFunction<UserSession, Measurement, UserSession> function);
}
