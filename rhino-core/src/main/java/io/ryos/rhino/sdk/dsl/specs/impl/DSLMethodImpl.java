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

import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.specs.DSLItem;
import io.ryos.rhino.sdk.dsl.specs.DSLMethod;
import java.util.List;

public class DSLMethodImpl implements DSLItem, DSLMethod {

  private final String name;
  private final LoadDsl dsl;

  public DSLMethodImpl(String name, LoadDsl dsl) {
    this.name = name;
    this.dsl = dsl;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DSLItem getParent() {
    return null;
  }

  @Override
  public void setParent(DSLItem parent) {
    throw new UnsupportedOperationException("DSL Method is top-level instance.");
  }

  @Override
  public boolean hasParent() {
    return false;
  }

  @Override
  public String getParentName() {
    return null;
  }

  @Override
  public List<DSLItem> getChildren() {
    return dsl.getChildren();
  }

  @Override
  public LoadDsl getDsl() {
    return dsl;
  }
}
