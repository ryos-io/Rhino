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
import io.ryos.rhino.sdk.dsl.mat.SomeSpecMaterializer;
import io.ryos.rhino.sdk.dsl.mat.SpecMaterializer;
import io.ryos.rhino.sdk.dsl.specs.DSLItem;
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.SomeSpec;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.Validate;

/**
 * DSLSpec implementation for arbitrary code execution.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class SomeSpecImpl extends AbstractMeasurableSpec implements SomeSpec {

  private Function<UserSession, String> function;

  public SomeSpecImpl(final String name) {
    super(Validate.notNull(name));
  }

  @Override
  public DSLSpec as(final Function<UserSession, String> function) {
    Validate.notNull(function, "function must not bu null.");
    this.function = function;
    return this;
  }

  @Override
  public Function<UserSession, String> getFunction() {
    return function;
  }

  @Override
  public SpecMaterializer<? extends DSLSpec> createMaterializer(UserSession session) {
    return new SomeSpecMaterializer();
  }

  @Override
  public List<DSLItem> getChildren() {
    return Collections.emptyList();
  }
}
