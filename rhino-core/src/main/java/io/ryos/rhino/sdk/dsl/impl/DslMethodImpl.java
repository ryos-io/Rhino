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

import io.ryos.rhino.sdk.dsl.DslItem;
import io.ryos.rhino.sdk.dsl.DslMethod;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.mat.DslMethodMaterializer;
import java.util.List;
import org.apache.commons.lang3.Validate;

public class DslMethodImpl implements DslItem, DslMethod {

  private String name;
  private DslItem dsl;
  private DslItem parent;

  public DslMethodImpl(String name, DslItem dsl) {
    this.name = Validate.notNull(name, "Name must not be null.");
    this.dsl = Validate.notNull(dsl, "DSL must not be null.");
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public DslItem getParent() {
    return parent;
  }

  @Override
  public void setParent(DslItem parent) {
    this.parent = parent;
  }

  @Override
  public boolean hasParent() {
    return parent != null;
  }

  @Override
  public String getParentName() {
    return parent != null ? parent.getParentName() : "";
  }

  @Override
  public List<MaterializableDslItem> getChildren() {
    return dsl.getChildren();
  }

  @Override
  public DslMethodMaterializer materializer() {
    return new DslMethodMaterializer(this);
  }

  @Override
  public DslItem getDsl() {
    return dsl;
  }

  @Override
  public String toString() {
    return "DslMethodImpl{" +
        "name='" + name + '\'' +
        '}';
  }
}
