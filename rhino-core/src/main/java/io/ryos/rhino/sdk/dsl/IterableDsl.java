package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.data.builder.ForEachBuilder;
import java.util.function.Predicate;

/**
 * Load DSL to describe iterable operations.
 *
 * @author Erhan Bagdemir
 */
public interface IterableDsl extends LoadDsl {

  /**
   * For-each DSL spec loops through the sequence of elements built by {@link ForEachBuilder}
   * instance.
   *
   * @param name           Name of the runner DSL.
   * @param forEachBuilder Iterable builder.
   * @return {@link RunnableDsl} runnable DSL instance.
   */
  <E, R extends Iterable<E>> RunnableDsl forEach(String name,
      ForEachBuilder<E, R> forEachBuilder);

  /**
   * Runs the {@link MaterializableDslItem} till the {@link Predicate} holds.
   *
   * @param predicate Run conditional.
   * @param spec      {@link MaterializableDslItem} to run.
   * @return {@link RunnableDsl} runnable DSL instance.
   */
  RunnableDsl until(Predicate<UserSession> predicate, MaterializableDslItem spec);

  /**
   * Runs the {@link MaterializableDslItem} as long as the {@link Predicate} holds.
   *
   * @param predicate Run conditional.
   * @param spec      {@link MaterializableDslItem} to run.
   * @return {@link RunnableDsl} runnable DSL instance.
   */
  RunnableDsl asLongAs(Predicate<UserSession> predicate, MaterializableDslItem spec);

  /**
   * Runs the {@link MaterializableDslItem} repeatedly.
   *
   * @param spec {@link MaterializableDslItem} to run.
   * @return {@link RunnableDsl} runnable DSL instance.
   */
  RunnableDsl repeat(MaterializableDslItem spec);
}
