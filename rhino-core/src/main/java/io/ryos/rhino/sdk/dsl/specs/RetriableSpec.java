package io.ryos.rhino.sdk.dsl.specs;

import java.util.function.Predicate;

/**
 * Retriable spec is the DSL spec which is to be retried if predicate turns true.
 * <p>
 *
 * @param <R> Return type.
 * @param <T> Predicate's type.
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface RetriableSpec<R extends MeasurableSpec, T> extends Spec {

  /**
   * Retries, if the predicate is true and the current attempt less then numOfRetries.
   * <p>
   *
   * @param predicate If predicate turns true, then the spec will be repeated.
   * @param numOfRetries Number of retries.
   * @return The spec instance which is to be repeated.
   */
  R retryIf(Predicate<T> predicate, int numOfRetries);
}
