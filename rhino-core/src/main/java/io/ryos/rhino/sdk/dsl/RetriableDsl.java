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

import java.util.function.Predicate;

/**
 * Retriable spec is the DSL spec which is to be retried if predicate turns true.
 * <p>
 *
 * @param <R> Return type.
 * @param <T> Predicate's type.
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface RetriableDsl<R extends MeasurableDsl, T> extends MaterializableDslItem {

  /**
   * Retries, if the predicate is true and the current attempt less then numOfRetries.
   * <p>
   *
   * @param predicate If predicate turns true, then the spec will be repeated.
   * @param numOfRetries Number of retries.
   * @return The spec instance which is to be repeated.
   */
  R retryIf(Predicate<T> predicate, int numOfRetries);
}
