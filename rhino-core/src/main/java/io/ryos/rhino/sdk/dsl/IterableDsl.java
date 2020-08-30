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
import io.ryos.rhino.sdk.dsl.SessionDslItem.Scope;
import io.ryos.rhino.sdk.dsl.data.builder.ForEachBuilder;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Load DSL to describe iterable operations.
 *
 * @author Erhan Bagdemir
 */
public interface IterableDsl extends DslItem {

  <E, R extends Iterable<E>, T extends MaterializableDslItem> DslBuilder forEach(
      Function<UserSession, R> iterableExtractor,
      Function<E, T> dslItemExtractor, String sessionKey, Scope scope);

  <E, R extends Iterable<E>, T extends MaterializableDslItem> DslBuilder forEach(
      R iterable,
      Function<E, T> dslItemExtractor, String sessionKey, Scope scope);

  <E, R extends Iterable<E>, T extends MaterializableDslItem> DslBuilder forEach(
      R iterable, Function<E, T> dslItemExtractor);

  <E, R extends Iterable<E>, T extends MaterializableDslItem> DslBuilder forEach(
      R iterable, Function<E, T> dslItemExtractor, String sessionKey);

  <E, R extends Iterable<E>, T extends MaterializableDslItem> DslBuilder forEach(
      final Function<UserSession, R> iterableExtractor,
      final Function<E, T> dslItemExtractor,
      final String sessionKey);

  <E, R extends Iterable<E>, T extends MaterializableDslItem> DslBuilder forEach(
      final Function<UserSession, R> iterableExtractor,
      final Function<E, T> dslItemExtractor);

  /**
   * For-each DSL spec loops through the sequence of elements built by {@link ForEachBuilder}
   * instance.
   *
   * @param forEachBuilder Iterable builder.
   * @return {@link DslBuilder} runnable DSL instance.
   */
  <E, R extends Iterable<E>, T extends MaterializableDslItem> DslBuilder forEach(
      ForEachBuilder<E, R, T> forEachBuilder);

  /**
   * Runs the {@link MaterializableDslItem} till the {@link Predicate} holds.
   *
   * @param predicate Run conditional.
   * @param spec      {@link MaterializableDslItem} to run.
   * @return {@link DslBuilder} runnable DSL instance.
   */
  DslBuilder until(Predicate<UserSession> predicate, MaterializableDslItem spec);

  /**
   * Runs the {@link MaterializableDslItem} as long as the {@link Predicate} holds.
   *
   * @param predicate Run conditional.
   * @param spec      {@link MaterializableDslItem} to run.
   * @return {@link DslBuilder} runnable DSL instance.
   */
  DslBuilder asLongAs(Predicate<UserSession> predicate, MaterializableDslItem spec);

  /**
   * Runs the {@link MaterializableDslItem} repeatedly.
   *
   * @param spec {@link MaterializableDslItem} to run.
   * @return {@link DslBuilder} runnable DSL instance.
   */
  DslBuilder repeat(MaterializableDslItem spec);

  DslBuilder repeat(MaterializableDslItem spec, int times);
}
