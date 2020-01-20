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

package io.ryos.rhino.sdk.dsl.impl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.mat.ForEachDslMaterializer;
import io.ryos.rhino.sdk.dsl.mat.DslMaterializer;
import io.ryos.rhino.sdk.dsl.DslItem;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.ForEachDsl;
import java.util.List;
import java.util.function.Function;

/**
 * For-each loop representation.
 *
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public class ForEachDslImpl<S, R extends Iterable<S>> extends AbstractSessionDslItem implements
    ForEachDsl {

  /**
   * Child DSL items.
   */
  private List<DslItem> children;

  /**
   * Function is a provider of iterable object instances.
   */
  private Function<UserSession, R> iterableSupplier;

  /**
   * For each function.
   */
  private Function<S, ? extends MaterializableDslItem> forEachFunction;

  /**
   * Constructs a new {@link ForEachDsl} instance.
   *
   * @param name             Spec name.
   * @param children         Child DSL items.
   * @param sessionKey       Session key.
   * @param scope            Session scope.
   * @param iterableSupplier Supplier for iterable.
   */
  public ForEachDslImpl(final String name,
      final List<DslItem> children,
      final String sessionKey,
      final Scope scope,
      final Function<UserSession, R> iterableSupplier,
      final Function<S, ? extends MaterializableDslItem> forEachFunction) {

    super(name, sessionKey, scope);

    this.children = children;
    this.iterableSupplier = iterableSupplier;
    this.forEachFunction = forEachFunction;
  }

  @Override
  public DslMaterializer<? extends MaterializableDslItem> materializer(UserSession session) {
    return new ForEachDslMaterializer();
  }

  @Override
  public List<DslItem> getChildren() {
    return children;
  }

  @Override
  public Function<UserSession, R> getIterableSupplier() {
    return iterableSupplier;
  }

  @Override
  public Function<S, ? extends MaterializableDslItem> getForEachFunction() {
    return forEachFunction;
  }
}
