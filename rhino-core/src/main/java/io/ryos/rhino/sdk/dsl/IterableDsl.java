package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.Spec;
import io.ryos.rhino.sdk.dsl.specs.impl.ForEachBuilder;
import java.util.function.Predicate;

/**
 * Load DSL to describe iterable operations.
 *
 * @author Erhan Bagdemir
 */
public interface IterableDsl extends LoadDsl {

  /**
   * For-each DSL spec loops through the sequence of elements built by {@link ForEachBuilder} instance.
   *
   * @param forEachBuilder Iterable builder.
   * @return {@link RunnableDsl} runnable DSL instance.
   */
  <E, R extends Iterable<E>> RunnableDsl forEach(ForEachBuilder<E, R> forEachBuilder);

  /**
   * Runs the {@link Spec} till the {@link Predicate} holds.
   *
   * @param predicate Run conditional.
   * @param spec {@link Spec} to run.
   * @return {@link RunnableDsl} runnable DSL instance.
   */
  RunnableDsl runUntil(Predicate<UserSession> predicate, Spec spec);

  /**
   * Runs the {@link Spec} as long as the {@link Predicate} holds.
   *
   * @param predicate Run conditional.
   * @param spec {@link Spec} to run.
   * @return {@link RunnableDsl} runnable DSL instance.
   */
  RunnableDsl runAsLongAs(Predicate<UserSession> predicate, Spec spec);

  /**
   * Runs the {@link Spec} repeatedly.
   *
   * @param spec {@link Spec} to run.
   * @return {@link RunnableDsl} runnable DSL instance.
   */
  RunnableDsl repeat(Spec spec);
}
