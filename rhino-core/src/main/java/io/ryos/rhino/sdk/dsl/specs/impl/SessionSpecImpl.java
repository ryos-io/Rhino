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
import io.ryos.rhino.sdk.dsl.mat.SessionSpecMaterializer;
import io.ryos.rhino.sdk.dsl.mat.SpecMaterializer;
import io.ryos.rhino.sdk.dsl.specs.SessionSpec;
import io.ryos.rhino.sdk.dsl.specs.Spec;
import java.util.function.Supplier;

public class SessionSpecImpl extends AbstractSpec implements SessionSpec {

  private final String key;
  private final Supplier<Object> objectSupplier;

  public SessionSpecImpl(String key,
      Supplier<Object> objectSupplier) {
    super("");

    this.key = key;
    this.objectSupplier = objectSupplier;
  }

  public String getKey() {
    return key;
  }

  public Supplier<Object> getObjectFunction() {
    return objectSupplier;
  }

  @Override
  public SpecMaterializer<? extends Spec> createMaterializer(UserSession session) {
    return new SessionSpecMaterializer();
  }
}
