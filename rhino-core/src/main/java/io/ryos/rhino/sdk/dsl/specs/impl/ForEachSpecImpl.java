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

package io.ryos.rhino.sdk.dsl.specs.impl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.mat.ForEachSpecMaterializer;
import io.ryos.rhino.sdk.dsl.mat.SpecMaterializer;
import io.ryos.rhino.sdk.dsl.specs.DSLItem;
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.ForEachSpec;
import java.util.List;
import java.util.function.Function;

/**
 * For-each loop representation.
 *
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public class ForEachSpecImpl<S, R extends Iterable<S>> extends AbstractSessionDSLItem implements
    ForEachSpec {

  /**
   * Child DSL items.
   */
  private List<DSLItem> children;

  /**
   * Function is a provider of iterable object instances.
   */
  private Function<UserSession, R> iterableSupplier;

  /**
   * For each function.
   */
  private Function<S, ? extends DSLSpec> forEachFunction;

  /**
   * Constructs a new {@link ForEachSpec} instance.
   *
   * @param name             Spec name.
   * @param children         Child DSL items.
   * @param sessionKey       Session key.
   * @param scope            Session scope.
   * @param iterableSupplier Supplier for iterable.
   */
  public ForEachSpecImpl(final String name,
      final List<DSLItem> children,
      final String sessionKey,
      final Scope scope,
      final Function<UserSession, R> iterableSupplier,
      final Function<S, ? extends DSLSpec> forEachFunction) {

    super(name, sessionKey, scope);

    this.children = children;
    this.iterableSupplier = iterableSupplier;
    this.forEachFunction = forEachFunction;
  }

  @Override
  public SpecMaterializer<? extends DSLSpec> createMaterializer(UserSession session) {
    return new ForEachSpecMaterializer();
  }

  @Override
  public List<DSLItem> getChildren() {
    return children;
  }

  @Override
  public Function<UserSession, R> getIterableSupplier() {
    return iterableSupplier;
  }

  @Override
  public Function<S, ? extends DSLSpec> getForEachFunction() {
    return forEachFunction;
  }
}
