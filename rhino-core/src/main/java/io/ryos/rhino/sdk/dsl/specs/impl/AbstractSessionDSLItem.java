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

import io.ryos.rhino.sdk.dsl.specs.SessionDSLItem;
import java.util.function.Supplier;

public abstract class AbstractSessionDSLItem extends AbstractMeasurableSpec implements
    SessionDSLItem {

  private Scope sessionScope = Scope.USER;
  private String sessionKey = "result";
  private Supplier<Object> objectSupplier;

  public AbstractSessionDSLItem(String name) {
    super(name);
  }

  @Override
  public Scope getSessionScope() {
    return sessionScope;
  }

  @Override
  public void setSessionScope(Scope sessionScope) {
    this.sessionScope = sessionScope;
  }

  @Override
  public String getKey() {
    return sessionKey;
  }

  @Override
  public Supplier<Object> getObjectFunction() {
    return objectSupplier;
  }

  public void setSessionKey(String sessionKey) {
    this.sessionKey = sessionKey;
  }

  public void setObjectSupplier(Supplier<Object> objectSupplier) {
    this.objectSupplier = objectSupplier;
  }
}
