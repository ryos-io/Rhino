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
import io.ryos.rhino.sdk.dsl.ExpressionDsl;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.ResultingDsl;
import io.ryos.rhino.sdk.dsl.mat.DslMaterializer;
import io.ryos.rhino.sdk.dsl.mat.ExpressionDslMaterializer;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ExpressionDslImpl extends AbstractDSLItem implements ExpressionDsl {

  private Consumer<UserSession> expression;

  public ExpressionDslImpl(Consumer<UserSession> expression) {
    super("");

    this.expression = expression;
  }

  @Override
  public List<MaterializableDslItem> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public <T extends MaterializableDslItem> DslMaterializer materializer() {
    return new ExpressionDslMaterializer(this);
  }

  public Consumer<UserSession> getExpression() {
    return expression;
  }

  @Override
  public UserSession handleResult(UserSession userSession, Object returnValue) {
    if (getParent() instanceof ResultingDsl) {
      ((ResultingDsl) getParent()).handleResult(userSession, returnValue);
    }
    return userSession;
  }

  @Override
  public String getSaveTo() {
    return null;
  }
}
