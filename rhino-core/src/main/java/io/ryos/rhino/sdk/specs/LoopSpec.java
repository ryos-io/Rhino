package io.ryos.rhino.sdk.specs;

/**
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public interface LoopSpec<E, R extends Iterable<E>> extends Spec {

  LoopBuilder<E, R> getLoopBuilder();
}
