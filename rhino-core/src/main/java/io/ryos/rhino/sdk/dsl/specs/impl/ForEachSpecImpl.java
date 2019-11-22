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
import io.ryos.rhino.sdk.dsl.specs.builder.ForEachBuilder;
import io.ryos.rhino.sdk.dsl.specs.builder.ForEachBuilderImpl;
import java.util.Collections;
import java.util.List;

/**
 * For-each loop representation.
 *
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public class ForEachSpecImpl<S, R extends Iterable<S>> extends AbstractSessionDSLItem implements
    ForEachSpec {

  /**
   * Builder implementation for {@link ForEachSpec}.
   */
  private final ForEachBuilder<S, R> forEachBuilder;

  /**
   * Constructs a new {@link ForEachSpec} instance.
   *
   * @param forEachBuilder Builder implementation for {@link ForEachSpec}.
   */
  public ForEachSpecImpl(String name, ForEachBuilder<S, R> forEachBuilder) {
    super(name);

    this.forEachBuilder = forEachBuilder;

    forEachBuilder.setSpec(this);

    setSessionKey(forEachBuilder.getKey() != null ? forEachBuilder.getKey() : name);
    setSessionScope(forEachBuilder.getScope());
  }

  /**
   * Getter for {@link ForEachBuilderImpl}.
   *
   * @return {@link ForEachBuilderImpl} instance.
   */
  public ForEachBuilder<S, R> getForEachBuilder() {
    return forEachBuilder;
  }

  public String getContextKey() {
    return getKey();
  }

  @Override
  public SpecMaterializer<? extends DSLSpec> createMaterializer(UserSession session) {
    return new ForEachSpecMaterializer();
  }

  @Override
  public List<DSLItem> getChildren() {
    return Collections.emptyList();
  }
}
