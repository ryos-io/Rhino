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

package io.ryos.rhino.sdk.dsl.specs.builder;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.Spec;
import io.ryos.rhino.sdk.dsl.specs.Spec.Scope;
import java.util.function.Function;

/**
 * Loop builder is a builder providing the spec with looping information to be executed.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public class ForEachBuilderImpl<E, R extends Iterable<E>> implements ForEachBuilder<E, R> {

  private String key;
  private String saveTo;
  private Scope scope;
  private Function<E, Spec> forEachFunction;
  private Function<UserSession, E> sessionExtractor;

  public ForEachBuilderImpl(final String key) {
    this.key = key;
  }

  public ForEachBuilderImpl(final Function<UserSession, E> sessionExtractor) {
    this.sessionExtractor = sessionExtractor;
  }

  public static <E, R extends Iterable<E>> ForEachBuilder<E, R> in(final String key) {
    return new ForEachBuilderImpl<>(key);
  }

  public static <E, R extends Iterable<E>> ForEachBuilder<E, R> in(
      Function<UserSession, E> sessionFunction) {
    return new ForEachBuilderImpl<>(sessionFunction);
  }

  @Override
  public ForEachBuilder<E, R> doRun(final Function<E, Spec> forEachFunction) {
    this.forEachFunction = forEachFunction;
    return this;
  }

  @Override
  public ForEachBuilder<E, R> saveTo(final String saveTo) {
    this.saveTo = saveTo;
    return this;
  }

  @Override
  public ForEachBuilder<E, R> saveTo(String saveTo, Scope scope) {
    this.saveTo = saveTo;
    this.scope = scope;
    return this;
  }

  public Scope getScope() {
    return scope;
  }

  public String getKey() {
    return key;
  }

  public String getSaveTo() {
    return saveTo;
  }

  public Function<E, Spec> getForEachFunction() {
    return forEachFunction;
  }

  public Function<UserSession, E> getSessionExtractor() {
    return sessionExtractor;
  }
}
