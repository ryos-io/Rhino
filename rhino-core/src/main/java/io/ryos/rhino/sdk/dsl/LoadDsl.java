package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.dsl.specs.DSLItem;
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.builder.MapperBuilder;

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
public interface LoadDsl extends DSLItem {

  <R, T> RunnableDsl map(MapperBuilder<R, T> mapper);
}
