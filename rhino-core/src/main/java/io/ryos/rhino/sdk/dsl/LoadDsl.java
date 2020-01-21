package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.impl.LoadDslImpl;
import java.time.Duration;
import java.util.function.Predicate;

/**
 * Runnable DSL is a {@link LoadDsl} instance which is used to describe executable steps.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface LoadDsl extends DslItem, SessionDsl, IterableDsl, AssertionDsl, MappableDsl {

  public static ThreadLocal<String> dslMethodName = new ThreadLocal<>();

  /**
   * Conditional runnable DSL is a {@link LoadDsl} if {@link Predicate} returns {@code true}, then
   * the execution proceeds and it runs the {@link MaterializableDslItem} passed as parameter.
   * <p>
   *
   * @param spec      {@link MaterializableDslItem} to materialize and run.
   * @param predicate {@link Predicate} which is conditional for execution of {@link
   *                  MaterializableDslItem} provided.
   * @return {@link LoadDslImpl} instance.
   */
  LoadDsl runIf(Predicate<UserSession> predicate, MaterializableDslItem spec);

  /**
   * Wait DSL is a DSL instance which makes execution halt for {@link Duration}.
   * <p>
   *
   * @param duration {@link Duration} to wait.
   * @return {@link LoadDslImpl} instance.
   */
  LoadDslImpl wait(Duration duration);

  /**
   * Runner DSL is a {@link LoadDsl} instance to run the {@link MaterializableDslItem} passed as
   * parameter.
   * <p>
   *
   * @param spec {@link MaterializableDslItem} to materialize and run.
   * @return {@link LoadDslImpl} instance.
   */
  LoadDsl run(MaterializableDslItem spec);

  public static LoadDsl dsl() {
    return new LoadDslImpl(dslMethodName.get());
  }
}
