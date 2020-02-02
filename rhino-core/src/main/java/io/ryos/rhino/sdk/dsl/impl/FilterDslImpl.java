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

package io.ryos.rhino.sdk.dsl.impl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.FilterDsl;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.mat.FilterDslMaterializer;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang3.Validate;

/**
 * @inheritDoc
 */
public class FilterDslImpl extends AbstractMeasurableDsl implements FilterDsl {

  private final Predicate<UserSession> predicate;

  public FilterDslImpl(final Predicate<UserSession> predicate) {
    super("");

    this.predicate = Validate.notNull(predicate);
  }

  @Override
  public List<MaterializableDslItem> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public FilterDslMaterializer materializer() {
    return new FilterDslMaterializer(this);
  }

  /**
   * @inheritDoc
   */
  public Predicate<UserSession> getPredicate() {
    return predicate;
  }
}
