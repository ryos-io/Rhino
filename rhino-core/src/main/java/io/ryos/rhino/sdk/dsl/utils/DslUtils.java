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
import io.ryos.rhino.sdk.dsl.HttpConfigDsl;
import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.SomeDsl;
import io.ryos.rhino.sdk.dsl.data.builder.ForEachBuilder;
import io.ryos.rhino.sdk.dsl.data.builder.MapperBuilder;
import io.ryos.rhino.sdk.dsl.impl.HttpDslImpl;
import io.ryos.rhino.sdk.dsl.impl.LoadDslImpl;
import io.ryos.rhino.sdk.dsl.impl.SomeDslImpl;
import io.ryos.rhino.sdk.dsl.mat.HttpDslData;
import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Contains static methods to make DSL more readable.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class DslUtils {

  public static final String DEFAULT_RESULT_OBJ = "result";

  private DslUtils() {
  }

  /**
   * Used as predicate to conditional DSL components:
   * <pre>
   *   until(ifStatusCode(200), http("Request"));
   * </pre>
   * <p>
   * Default session key for expected Http response is "result".
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

  public static LoadDsl runIf(Predicate<UserSession> predicate, MaterializableDslItem matDslItem) {
    return new LoadDslImpl(LoadDsl.dslMethodName.get()).runIf(predicate, matDslItem);
  }

  public static LoadDsl run(MaterializableDslItem matDslItem) {
    return new LoadDslImpl(LoadDsl.dslMethodName.get()).run(matDslItem);
  }

  public static LoadDsl wait(Duration duration) {
    return new LoadDslImpl(LoadDsl.dslMethodName.get()).wait(duration);
  }

  public static LoadDsl ensure(Predicate<UserSession> predicate) {
    return new LoadDslImpl(LoadDsl.dslMethodName.get()).ensure(predicate);
  }

  public static LoadDsl ensure(Predicate<UserSession> predicate, String reason) {
    return new LoadDslImpl(LoadDsl.dslMethodName.get()).ensure(predicate, reason);
  }

  public static LoadDsl session(String sessionKey, Supplier<Object> objectSupplier) {
    return new LoadDslImpl(LoadDsl.dslMethodName.get()).session(sessionKey, objectSupplier);
  }

  public static <R, T> LoadDsl map(MapperBuilder<R, T> mapperBuilder) {
    return new LoadDslImpl(LoadDsl.dslMethodName.get()).map(mapperBuilder);
  }

  public static <E, R extends Iterable<E>> LoadDsl forEach(String name,
      ForEachBuilder<E, R> forEachBuilder) {
    return new LoadDslImpl(LoadDsl.dslMethodName.get()).forEach(name, forEachBuilder);
  }

  public static LoadDsl repeat(MaterializableDslItem spec) {
    return new LoadDslImpl(LoadDsl.dslMethodName.get()).repeat(spec);
  }

  public static LoadDsl until(Predicate<UserSession> predicate, MaterializableDslItem dslItem) {
    return new LoadDslImpl(LoadDsl.dslMethodName.get()).until(predicate, dslItem);
  }

  public static LoadDsl asLongAs(Predicate<UserSession> predicate, MaterializableDslItem dslItem) {
    return new LoadDslImpl(LoadDsl.dslMethodName.get()).asLongAs(predicate, dslItem);
  }

  public static LoadDsl filter(Predicate<UserSession> predicate) {
    return new LoadDslImpl(LoadDsl.dslMethodName.get()).filter(predicate);
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
}
