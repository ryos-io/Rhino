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
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.ForEachSpec;
import io.ryos.rhino.sdk.dsl.specs.SessionDSLItem.Scope;
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
  private Scope scope = Scope.EPHEMERAL;
  private Function<E, ? extends DSLSpec> forEachFunction;
  private Function<UserSession, R> sessionExtractor;
  private ForEachSpec<E, R> forEachSpec;

  public ForEachBuilderImpl(final String key) {
    this.key = key;
  }

  public ForEachBuilderImpl(final Function<UserSession, R> sessionExtractor) {
    this.sessionExtractor = sessionExtractor;
  }

  public static <E, R extends Iterable<E>> ForEachBuilder<E, R> in(final String key) {
    return new ForEachBuilderImpl<>(key);
  }

  public static <E, R extends Iterable<E>> ForEachBuilder<E, R> in(
      Function<UserSession, R> sessionFunction) {
    return new ForEachBuilderImpl<>(sessionFunction);
  }

  @Override
  public ForEachBuilder<E, R> doRun(final Function<E, DSLSpec> forEachFunction) {
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

  @Override
  public ForEachSpec<E, R> getSpec() {
    return forEachSpec;
  }

  @Override
  public void setSpec(ForEachSpec<E, R> spec) {
    this.forEachSpec = spec;
  }

  @Override
  public String getKey() {
    return key;
  }

  public String getSaveTo() {
    return saveTo;
  }

  @Override
  public Function<E, ? extends DSLSpec> getForEachFunction() {
    return forEachFunction;
  }

  @Override
  public Function<UserSession, R> getSessionExtractor() {
    return sessionExtractor;
  }
}
