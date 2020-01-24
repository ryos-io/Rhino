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

package io.ryos.rhino.sdk.dsl.data.builder;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.DslItem;
import io.ryos.rhino.sdk.dsl.ForEachDsl;
import io.ryos.rhino.sdk.dsl.SessionDslItem.Scope;
import java.util.function.Function;
import org.apache.commons.lang3.Validate;

/**
 * Loop builder is a builder providing the spec with looping information to be executed.
 *
 * @author Erhan Bagdemir
 */
public class ForEachBuilderImpl<E, R extends Iterable<E>> implements ForEachBuilder<E, R> {

  private String sessionKey;
  private Scope scope = Scope.EPHEMERAL;
  private Function<E, ? extends DslItem> forEachFunction;
  private Function<UserSession, R> iterableSupplier;
  private ForEachDsl<E, R> forEachDsl;
  private Function<E, Object> mapper;

  public ForEachBuilderImpl(final String sessionKey) {
    this.sessionKey = sessionKey;
  }

  public ForEachBuilderImpl(final Function<UserSession, R> iterableSupplier) {
    this.iterableSupplier = iterableSupplier;
  }

  public static <E, R extends Iterable<E>> ForEachBuilder<E, R> in(final String sessionKey) {
    Validate.notEmpty(sessionKey, "Session key must not be empty.");
    return new ForEachBuilderImpl<>(sessionKey);
  }

  public static <E, R extends Iterable<E>> ForEachBuilder<E, R> in(
      Function<UserSession, R> iterableSupplier) {
    Validate.notNull(iterableSupplier, "Iterable supplier must not be null.");
    return new ForEachBuilderImpl<>(iterableSupplier);
  }

  public static <E, R extends Iterable<E>> ForEachBuilder<E, R> in(R iterable) {
    Validate.notNull(iterable, "Iterable must not be null.");
    return new ForEachBuilderImpl<>(session -> iterable);
  }

  @Override
  public ForEachBuilder<E, R> exec(final Function<E, DslItem> forEachFunction) {
    Validate.notNull(forEachFunction, "forEachFunction must not be null.");
    this.forEachFunction = forEachFunction;
    return this;
  }

  @Override
  public ForEachBuilder<E, R> map(final Function<E, Object> mapper) {
    Validate.notNull(mapper, "mapper must not be null.");
    this.mapper = mapper;
    return this;
  }

  @Override
  public ForEachBuilder<E, R> saveTo(final String sessionKey) {
    Validate.notEmpty(sessionKey, "Session key must not be empty.");
    this.sessionKey = sessionKey;
    return this;
  }

  @Override
  public ForEachBuilder<E, R> saveTo(String sessionKey, Scope scope) {
    Validate.notEmpty(sessionKey, "Session key must not be empty.");
    Validate.notNull(scope, "Scope must not be null.");
    this.sessionKey = sessionKey;
    this.scope = scope;
    return this;
  }

  public Scope getSessionScope() {
    return scope;
  }

  @Override
  public ForEachDsl<E, R> getSpec() {
    return forEachDsl;
  }

  @Override
  public void setSpec(ForEachDsl<E, R> spec) {
    this.forEachDsl = spec;
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Function<E, ? extends DslItem> getForEachFunction() {
    return forEachFunction;
  }

  public Function<E, Object> getMapper() {
    return mapper;
  }

  @Override
  public Function<UserSession, R> getIterableSupplier() {
    return iterableSupplier;
  }
}
