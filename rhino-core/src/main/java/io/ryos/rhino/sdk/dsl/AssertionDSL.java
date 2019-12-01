package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import java.util.function.Predicate;

public interface AssertionDSL {

  /**
   * Ensure DSL is to assert the predicate passed holds true, otherwise it stops the pipeline.
   *
   * @return {@link LoadDslImpl} instance.
   */
  RunnableDsl ensure(Predicate<UserSession> predicate);

  /**
   * Ensure DSL is to assert the predicate passed holds true, otherwise it stops the pipeline.
   *
   * @return {@link LoadDslImpl} instance.
   */
  RunnableDsl ensure(Predicate<UserSession> predicate, String reason);
}