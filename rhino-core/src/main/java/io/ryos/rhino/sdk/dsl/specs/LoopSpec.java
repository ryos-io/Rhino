package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.dsl.specs.impl.LoopBuilder;

/**
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public interface LoopSpec<E, R extends Iterable<E>> extends Spec {

  LoopBuilder<E, R> getLoopBuilder();
}
