package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.reporting.Measurement;
import io.ryos.rhino.sdk.specs.Spec;
import java.util.function.BiFunction;

/**
 * Some spec is a custom spec to enable developers to add arbitrary code snippets into the DSL.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface SomeSpec {

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
  Spec is(BiFunction<UserSession, Measurement, UserSession> function);
}
