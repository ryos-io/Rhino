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
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import java.util.function.Predicate;

/**
 * DSLSpec wrapper including a predicate to define the conditional statement whether a spec is to be
 * run, or not.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class ConditionalSpecWrapper extends AbstractMeasurableSpec {

  private final Predicate<UserSession> predicate;
  private final DSLSpec spec;

  public ConditionalSpecWrapper(DSLSpec spec, Predicate<UserSession> predicate) {
    super(spec.getName());

    this.spec = spec;
    this.predicate = predicate;
  }

  public Predicate<UserSession> getPredicate() {
    return predicate;
  }

  public DSLSpec getSpec() {
    return spec;
  }

  @Override
  public SpecMaterializer<? extends DSLSpec> createMaterializer(UserSession session) {
    return spec.createMaterializer(session);
  }
}
