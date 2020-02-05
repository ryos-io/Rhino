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
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.mat.ConditionalDslMaterializer;
import io.ryos.rhino.sdk.dsl.mat.DslMaterializer;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang3.Validate;

/**
 * MaterializableDslItem wrapper including a predicate to define the conditional statement whether a wrappedDslItem is to be
 * run, or not.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class ConditionalDslWrapper extends AbstractMeasurableDsl {

  private final Predicate<UserSession> predicate;
  private final MaterializableDslItem wrappedDslItem;

  public ConditionalDslWrapper(MaterializableDslItem wrappedDslItem,
      Predicate<UserSession> predicate) {
    super(wrappedDslItem.getName());

    this.wrappedDslItem = Validate.notNull(wrappedDslItem, "Spec must not be null.");
    this.predicate = Validate.notNull(predicate, "Predicate must not be null.");
  }

  public Predicate<UserSession> getPredicate() {
    return predicate;
  }

  public MaterializableDslItem getWrappedDslItem() {
    return wrappedDslItem;
  }

  @Override
  public DslMaterializer materializer() {
    return new ConditionalDslMaterializer(this);
  }

  @Override
  public List<MaterializableDslItem> getChildren() {
    return Collections.emptyList();
  }
}
