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
import io.ryos.rhino.sdk.dsl.mat.SpecMaterializer;
import io.ryos.rhino.sdk.dsl.specs.DslItem;
import io.ryos.rhino.sdk.dsl.specs.MaterializableDslItem;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang3.Validate;

/**
 * MaterializableDslItem wrapper including a predicate to define the conditional statement whether a spec is to be
 * run, or not.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class ConditionalDslWrapper extends AbstractMeasurableDsl {

  private final Predicate<UserSession> predicate;
  private final MaterializableDslItem spec;

  public ConditionalDslWrapper(MaterializableDslItem spec, Predicate<UserSession> predicate) {
    super(spec.getName());

    this.spec = Validate.notNull(spec, "Spec must not be null.");
    this.predicate = Validate.notNull(predicate, "Predicate must not be null.");
  }

  public Predicate<UserSession> getPredicate() {
    return predicate;
  }

  public MaterializableDslItem getSpec() {
    return spec;
  }

  @Override
  public SpecMaterializer<? extends MaterializableDslItem> materializer(UserSession session) {
    return spec.materializer(session);
  }

  @Override
  public List<DslItem> getChildren() {
    return Collections.emptyList();
  }
}
