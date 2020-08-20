/*
 * Copyright 2018 Ryos.io.
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

package io.ryos.rhino.sdk.dsl.utils;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.DslBuilder;
import io.ryos.rhino.sdk.dsl.ExpressionDsl;
import io.ryos.rhino.sdk.dsl.HttpConfigDsl;
import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.SomeDsl;
import io.ryos.rhino.sdk.dsl.data.builder.ForEachBuilder;
import io.ryos.rhino.sdk.dsl.data.builder.MapperBuilder;
import io.ryos.rhino.sdk.dsl.impl.DslBuilderImpl;
import io.ryos.rhino.sdk.dsl.impl.ExpressionDslImpl;
import io.ryos.rhino.sdk.dsl.impl.HttpDslImpl;
import io.ryos.rhino.sdk.dsl.impl.SomeDslImpl;
import io.ryos.rhino.sdk.dsl.mat.HttpDslData;
import io.ryos.rhino.sdk.reporting.VerificationInfo;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Contains static methods to make DSL more readable.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class DslUtils {

  private static final String DEFAULT_RESULT_OBJ = "result";

  private DslUtils() {
  }

  /**
   * Used as predicate to conditional DSL components:
   * <pre>
   *   until(ifStatusCode(200), http("Request"));
   * </pre>
   * <p>
   * Default define key for expected Http response is "result".
   *
   * @param statusCode Status code of the Http Response.
   * @return Predicate instance.
   */
  public static Predicate<UserSession> ifStatusCode(int statusCode) {
    return session ->
        session.<HttpDslData>get(DEFAULT_RESULT_OBJ)
            .map(httpDslData -> httpDslData.getResponse().getStatusCode())
            .orElse(-1) == statusCode;
  }

  public static DslBuilder runIf(Predicate<UserSession> predicate,
      MaterializableDslItem matDslItem) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).runIf(predicate, matDslItem);
  }

  public static DslBuilder run(MaterializableDslItem matDslItem) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).run(matDslItem);
  }

  public static DslBuilder wait(Duration duration) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).wait(duration);
  }

  public static DslBuilder ensure(Predicate<UserSession> predicate) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).ensure(predicate);
  }

  public static DslBuilder ensure(Predicate<UserSession> predicate, String reason) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).ensure(predicate, reason);
  }

  public static DslBuilder define(String sessionKey, Supplier<Object> valueSupplier) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).session(sessionKey, valueSupplier);
  }

  public static DslBuilder collect(String sessionKey, Supplier<Object> valueSupplier) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).session(sessionKey, valueSupplier);
  }

  public static <R, T> DslBuilder map(MapperBuilder<R, T> mapperBuilder) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).map(mapperBuilder);
  }

  public static <E, R extends Iterable<E>, T extends MaterializableDslItem> DslBuilder forEach(
      String name,
      ForEachBuilder<E, R, T> forEachBuilder) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).forEach(name, forEachBuilder);
  }

  public static <E, R extends Iterable<E>, T extends MaterializableDslItem> DslBuilder forEach(
      R iterable, Function<E, T> dslItemExtractor) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).forEach(iterable, dslItemExtractor);
  }

  public static DslBuilder repeat(MaterializableDslItem spec) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).repeat(spec);
  }

  public static DslBuilder repeat(MaterializableDslItem spec, int times) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).repeat(spec, times);
  }

  public static DslBuilder until(Predicate<UserSession> predicate, MaterializableDslItem dslItem) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).until(predicate, dslItem);
  }

  public static DslBuilder asLongAs(Predicate<UserSession> predicate,
      MaterializableDslItem dslItem) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).asLongAs(predicate, dslItem);
  }

  public static DslBuilder filter(Predicate<UserSession> predicate) {
    return new DslBuilderImpl(DslBuilder.dslMethodName.get()).filter(predicate);
  }

  /**
   * Static factory method to create a new {@link HttpDsl} instance.
   *
   * @param name Measurement point name.
   * @return A new instance of {@link MaterializableDslItem}.
   */
  public static HttpConfigDsl http(String name) {
    return new HttpDslImpl(name);
  }

  public static SomeDsl some(String name) {
    return new SomeDslImpl(name);
  }

  public static ExpressionDsl eval(Consumer<UserSession> expression) {
    return new ExpressionDslImpl(expression);
  }

  public static VerificationInfo<String> resulting(String state) {
    return new VerificationInfo<String>(state, (t) -> t.equals(state));
  }
}
