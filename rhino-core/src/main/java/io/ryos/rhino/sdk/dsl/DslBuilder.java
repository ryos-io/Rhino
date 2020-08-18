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
import io.ryos.rhino.sdk.dsl.impl.DslBuilderImpl;
import io.ryos.rhino.sdk.reporting.VerificationInfo;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * DSL is a {@link DslBuilder} instance which is used to describe executable steps.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public interface DslBuilder extends SessionDsl, IterableDsl, AssertionDsl, MappableDsl,
    MaterializableDslItem, MeasureDsl {

  ThreadLocal<String> dslMethodName = new ThreadLocal<>();

  public static DslBuilder dsl() {
    return new DslBuilderImpl("");
  }

  /**
   * Wait DSL is a DSL instance which makes execution halt for {@link Duration}.
   * <p>
   *
   * @param duration {@link Duration} to wait.
   * @return {@link DslBuilderImpl} instance.
   */
  DslBuilder wait(Duration duration);

  /**
   * Runner DSL is a {@link DslBuilder} instance to run the {@link MaterializableDslItem} passed as
   * parameter.
   * <p>
   *
   * @param spec {@link MaterializableDslItem} to materialize and run.
   * @return {@link DslBuilderImpl} instance.
   */
  DslBuilder run(MaterializableDslItem spec);

  /**
   * Conditional runnable DSL is a {@link DslBuilder} if {@link Predicate} returns {@code true}, then
   * the execution proceeds and it runs the {@link MaterializableDslItem} passed as parameter.
   * <p>
   *
   * @param spec      {@link MaterializableDslItem} to materialize and run.
   * @param predicate {@link Predicate} which is conditional for execution of {@link
   *                  MaterializableDslItem} provided.
   * @return {@link DslBuilderImpl} instance.
   */
  DslBuilder runIf(Predicate<UserSession> predicate, MaterializableDslItem spec);

  /**
   * Filter is used to filter according to the predicate.
   *
   * @param predicate Predicate instance applied in filter.
   * @return {@link DslBuilderImpl} instance.
   */
  DslBuilder filter(Predicate<UserSession> predicate);

  /**
   * Expression to evaluate. The processing time of running the expression will not be reported.
   *
   * @param expression A consumer taking a user session instance as parameter.
   * @return A DslBuilder instance.
   */
  DslBuilder eval(Consumer<UserSession> expression);


  /**
   * Verifies the results of the DSL expression. The verification result will be output in the
   * reports.
   *
   * @param dslItem          Verifiable DSL item, of which result will be verified.
   * @param verificationInfo Verification info contains the information how the verifiable DSL item
   *                         will be verified.
   * @return A DslBuilder instance.
   */
  <T> DslBuilder verify(VerifiableDslItem dslItem, VerificationInfo<T> verificationInfo);
}
