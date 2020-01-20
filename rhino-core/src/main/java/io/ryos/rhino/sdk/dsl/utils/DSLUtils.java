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
import io.ryos.rhino.sdk.dsl.mat.HttpSpecData;
import java.util.function.Predicate;

/**
 * Contains static methods to make DSL more readable.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 2.0.0
 */
public class DSLUtils {

  public static final String DEFAULT_RESULT_OBJ = "result";

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
        session.<HttpSpecData>get(DEFAULT_RESULT_OBJ)
            .map(httpSpecData -> httpSpecData.getResponse().getStatusCode())
            .orElse(-1) == statusCode;
  }
}
