package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.dsl.data.builder.MapperBuilder;

/**
 * Load DSL to describe load tests. The reactive runner materializes the DSL provided and run the
 * {@link MaterializableDslItem} instances registered.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner
 * @see MaterializableDslItem
 * @since 1.1.0
 */
public interface LoadDsl extends DslItem {

  <R, T> RunnableDsl map(MapperBuilder<R, T> mapper);
}
