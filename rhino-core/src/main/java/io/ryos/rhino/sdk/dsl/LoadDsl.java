package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.dsl.specs.Spec;
import io.ryos.rhino.sdk.dsl.specs.impl.ForEachBuilder;
import io.ryos.rhino.sdk.dsl.specs.impl.MapperBuilder;
import java.time.Duration;

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
   * @return {@link RunnableDslImpl} instance.
   */
  RunnableDslImpl wait(Duration duration);

  /**
   * Runs a {@link Spec} by materializing it.
   * <p>
   *
   * @param spec {@link Spec} to materialize and run.
   * @return {@link RunnableDslImpl} instance.
   */
  RunnableDsl run(Spec spec);

  <R, T> RunnableDsl map(MapperBuilder<R, T> mapper);

  <E, R extends Iterable<E>> RunnableDsl forEach(ForEachBuilder<E, R> forEachBuilder);
}
