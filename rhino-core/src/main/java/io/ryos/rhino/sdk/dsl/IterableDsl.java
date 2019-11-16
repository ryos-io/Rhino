package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.builder.ForEachBuilder;
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
   * @param forEachBuilder Iterable builder.
   * @return {@link RunnableDsl} runnable DSL instance.
   */
  <E, R extends Iterable<E>> RunnableDsl forEach(String contextKey,
      ForEachBuilder<E, R> forEachBuilder);

  /**
   * Runs the {@link DSLSpec} till the {@link Predicate} holds.
   *
   * @param predicate Run conditional.
   * @param spec {@link DSLSpec} to run.
   * @return {@link RunnableDsl} runnable DSL instance.
   */
  RunnableDsl runUntil(Predicate<UserSession> predicate, DSLSpec spec);

  /**
   * Runs the {@link DSLSpec} as long as the {@link Predicate} holds.
   *
   * @param predicate Run conditional.
   * @param spec {@link DSLSpec} to run.
   * @return {@link RunnableDsl} runnable DSL instance.
   */
  RunnableDsl runAsLongAs(Predicate<UserSession> predicate, DSLSpec spec);

  /**
   * Runs the {@link DSLSpec} repeatedly.
   *
   * @param spec {@link DSLSpec} to run.
   * @return {@link RunnableDsl} runnable DSL instance.
   */
  RunnableDsl repeat(DSLSpec spec);
}
