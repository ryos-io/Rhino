package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.specs.Spec;
import java.util.function.Predicate;

/**
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface ConfigurableDsl extends LoadDsl {


  /**
   * Runs a {@link Spec} by materializing it if {@link Predicate} returns true.
   * <p>
   *
   * @param spec {@link Spec} to materialize and run.
   * @param predicate {@link Predicate} which is conditional for execution of {@link Spec}
   * provided.
   * @return {@link ConnectableDsl} instance.
   */
  ConfigurableDsl runIf(Predicate<UserSession> predicate, Spec spec);
}
