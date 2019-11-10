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
import io.ryos.rhino.sdk.dsl.specs.EnsureSpec;
import io.ryos.rhino.sdk.dsl.specs.Spec;
import java.util.function.Predicate;

/**
 * Ensure spec implementation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class EnsureSpecImpl extends AbstractSpec implements EnsureSpec {

  private Predicate<UserSession> predicate;
  private String cause = "Ensure failed.";

  public EnsureSpecImpl(Predicate<UserSession> predicate) {
    this("", predicate);
  }

  public EnsureSpecImpl(Predicate<UserSession> predicate, String cause) {
    this("", predicate);
    this.cause = cause;
  }

  public EnsureSpecImpl(String measurement, Predicate<UserSession> predicate) {
    super(measurement);

    this.predicate = predicate;
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
  public SpecMaterializer<? extends Spec> createMaterializer(UserSession session) {
    return new EnsureSpecMaterializer();
  }
}
