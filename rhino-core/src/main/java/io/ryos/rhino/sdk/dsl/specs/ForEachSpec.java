package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.dsl.specs.builder.ForEachBuilder;

/**
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public interface ForEachSpec<E, R extends Iterable<E>> extends DSLSpec, SessionDSLItem {

  ForEachBuilder<E, R> getForEachBuilder();
}
