package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.specs.Spec;
import java.time.Duration;
import java.util.function.Predicate;

/**
 * Load DSL to describe load tests. The reactive runner materializes the DSL provided and run the
 * {@link Spec} instances registered.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner
 * @see Spec
 * @since 1.1.0
 */
public interface LoadDsl {

  /**
   * Wait for {@link Duration}.
   * <p>
   *
   * @param duration {@link Duration} to wait.
   * @return {@link ConnectableDsl} instance.
   */
  ConnectableDsl wait(Duration duration);

  /**
   * Runs a {@link Spec} by materializing it.
   * <p>
   *
   * @param spec {@link Spec} to materialize and run.
   * @return {@link ConnectableDsl} instance.
   */
  ConnectableDsl run(Spec spec);

  /**
   * Runs a {@link Spec} by materializing it if {@link Predicate} returns true.
   * <p>
   *
   * @param spec {@link Spec} to materialize and run.
   * @param predicate {@link Predicate} which is conditional for execution of {@link Spec}
   * provided.
   * @return {@link ConnectableDsl} instance.
   */
  ConnectableDsl runIf(Predicate<UserSession> predicate, Spec spec);
}
