package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.Spec;
import java.time.Duration;
import java.util.function.Predicate;

/**
 * Runnable DSL is a {@link LoadDsl} instance which is used to describe executable steps.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface RunnableDsl extends LoadDsl, IterableDsl {

  /**
   * Conditional runnable DSL is a {@link LoadDsl}if {@link Predicate} returns {@code true}, then
   * the execution proceeds and it runs the {@link Spec} passed as parameter.
   * <p>
   *
   * @param spec {@link Spec} to materialize and run.
   * @param predicate {@link Predicate} which is conditional for execution of {@link Spec}
   * provided.
   * @return {@link RunnableDslImpl} instance.
   */
  RunnableDsl runIf(Predicate<UserSession> predicate, Spec spec);

  /**
   * Wait DSL is a DSL instance which makes execution halt for {@link Duration}.
   * <p>
   *
   * @param duration {@link Duration} to wait.
   * @return {@link RunnableDslImpl} instance.
   */
  RunnableDslImpl wait(Duration duration);

  /**
   * Runner DSL is a {@link LoadDsl} instance to run the {@link Spec} passed as parameter.
   * <p>
   *
   * @param spec {@link Spec} to materialize and run.
   * @return {@link RunnableDslImpl} instance.
   */
  RunnableDsl run(Spec spec);

  /**
   * Ensure DSL is to assert the predicate passed holds true, otherwise it stops the pipeline.
   *
   * @return {@link RunnableDslImpl} instance.
   */
  RunnableDsl ensure(Predicate<UserSession> predicate);

  /**
   * Ensure DSL is to assert the predicate passed holds true, otherwise it stops the pipeline.
   *
   * @return {@link RunnableDslImpl} instance.
   */
  RunnableDsl ensure(Predicate<UserSession> predicate, String reason);
}
