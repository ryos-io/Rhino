package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.builder.MapperBuilder;
import java.time.Duration;

/**
 * Load DSL to describe load tests. The reactive runner materializes the DSL provided and run the
 * {@link DSLSpec} instances registered.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner
 * @see DSLSpec
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
  RunnableDsl wait(Duration duration);

  <R, T> RunnableDsl map(MapperBuilder<R, T> mapper);

}
