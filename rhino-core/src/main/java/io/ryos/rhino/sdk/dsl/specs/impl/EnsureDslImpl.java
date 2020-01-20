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
import io.ryos.rhino.sdk.dsl.mat.EnsureSpecMaterializer;
import io.ryos.rhino.sdk.dsl.mat.SpecMaterializer;
import io.ryos.rhino.sdk.dsl.specs.DslItem;
import io.ryos.rhino.sdk.dsl.specs.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.specs.EnsureDsl;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang3.Validate;

/**
 * Ensure spec implementation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class EnsureDslImpl extends AbstractMeasurableDsl implements EnsureDsl {

  private static final String BLANK = "";

  private Predicate<UserSession> predicate;
  private String cause = "Ensure failed.";

  public EnsureDslImpl(Predicate<UserSession> predicate) {
    this(BLANK, Validate.notNull(predicate, "Predicate must not be null."));
  }

  public EnsureDslImpl(Predicate<UserSession> predicate, String cause) {
    this(BLANK, Validate.notNull(predicate, "Predicate must not be null."));
    this.cause = Validate.notNull(cause, "Cause must not be null.");
  }

  public EnsureDslImpl(String measurement, Predicate<UserSession> predicate) {
    super(measurement);

    this.predicate = Validate.notNull(predicate, "Predicate must not be null.");
  }

  @Override
  public Predicate<UserSession> getPredicate() {
    return predicate;
  }

  @Override
  public String getCause() {
    return cause;
  }

  @Override
  public SpecMaterializer<? extends MaterializableDslItem> materializer(UserSession session) {
    return new EnsureSpecMaterializer();
  }

  @Override
  public List<DslItem> getChildren() {
    return Collections.emptyList();
  }
}
