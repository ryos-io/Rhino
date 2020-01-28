/*
 * Copyright 2020 Ryos.io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.impl.LoadDslImpl;
import java.time.Duration;
import java.util.function.Predicate;

/**
 * DSL is a {@link LoadDsl} instance which is used to describe executable steps.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public interface LoadDsl extends SessionDsl, IterableDsl, AssertionDsl, MappableDsl,
    MaterializableDslItem {

  ThreadLocal<String> dslMethodName = new ThreadLocal<>();

  public static LoadDsl dsl() {
    return new LoadDslImpl(dslMethodName.get());
  }

  /**
   * Wait DSL is a DSL instance which makes execution halt for {@link Duration}.
   * <p>
   *
   * @param duration {@link Duration} to wait.
   * @return {@link LoadDslImpl} instance.
   */
  LoadDsl wait(Duration duration);

  /**
   * Runner DSL is a {@link LoadDsl} instance to run the {@link MaterializableDslItem} passed as
   * parameter.
   * <p>
   *
   * @param spec {@link MaterializableDslItem} to materialize and run.
   * @return {@link LoadDslImpl} instance.
   */
  LoadDsl run(MaterializableDslItem spec);

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
   * Filter is used to filter according to the predicate.
   *
   * @param predicate Predicate instance applied in filter.
   * @return {@link LoadDslImpl} instance.
   */
  LoadDsl filter(Predicate<UserSession> predicate);
}
