package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import java.time.Duration;
import java.util.function.Predicate;

/**
 * Runnable DSL is a {@link LoadDsl} instance which is used to describe executable steps.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface RunnableDsl extends LoadDsl, SessionDSL, IterableDsl {

  /**
   * Conditional runnable DSL is a {@link LoadDsl}if {@link Predicate} returns {@code true}, then
   * the execution proceeds and it runs the {@link DSLSpec} passed as parameter.
   * <p>
   *
   * @param spec {@link DSLSpec} to materialize and run.
   * @param predicate {@link Predicate} which is conditional for execution of {@link DSLSpec}
   * provided.
   * @return {@link LoadDslImpl} instance.
   */
  RunnableDsl runIf(Predicate<UserSession> predicate, DSLSpec spec);

  /**
   * Wait DSL is a DSL instance which makes execution halt for {@link Duration}.
   * <p>
   *
   * @param duration {@link Duration} to wait.
   * @return {@link LoadDslImpl} instance.
   */
  LoadDslImpl wait(Duration duration);

  /**
   * Runner DSL is a {@link LoadDsl} instance to run the {@link DSLSpec} passed as parameter.
   * <p>
   *
   * @param spec {@link DSLSpec} to materialize and run.
   * @return {@link LoadDslImpl} instance.
   */
  RunnableDsl run(DSLSpec spec);
}
