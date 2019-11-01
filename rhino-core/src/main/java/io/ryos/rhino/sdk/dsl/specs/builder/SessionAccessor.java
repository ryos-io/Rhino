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

package io.ryos.rhino.sdk.dsl.specs.builder;

import io.ryos.rhino.sdk.data.UserSession;
import java.util.HashMap;
import java.util.function.Function;
import ognl.DefaultClassResolver;
import ognl.DefaultMemberAccess;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

public class SessionAccessor {

  public static <T> Function<UserSession, T> session(String key) {
    return (session) -> session.<T>get(key).orElseThrow(() -> new RuntimeException("Value with "
        + "key: " + key + " not found in session."));
  }

  public static <T> Function<UserSession, T> session(String key, String expression) {
    return (session) -> session.<T>get(key)
        .map(o -> readObject(expression, o))
        .orElseThrow(
            () -> new RuntimeException("Value with key: " + key + " not found in session."));
  }

  private static <T> T readObject(String expressionString, T object) {
    try {
      var expressionObject = Ognl.parseExpression(expressionString);
      var ctx = new OgnlContext(new DefaultClassResolver(),
          new DefaultTypeConverter(),
          new DefaultMemberAccess(true),
          new HashMap());
      return (T) Ognl.getValue(expressionObject, ctx, object);
    } catch (OgnlException e) {
      throw new RuntimeException(e);
    }
  }
}