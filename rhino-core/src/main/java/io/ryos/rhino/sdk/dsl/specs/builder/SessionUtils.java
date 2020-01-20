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
import io.ryos.rhino.sdk.dsl.specs.HttpDsl;
import io.ryos.rhino.sdk.dsl.specs.SessionDslItem.Scope;
import io.ryos.rhino.sdk.exceptions.SessionKeyNotFoundException;
import io.ryos.rhino.sdk.users.data.User;
import java.util.HashMap;
import java.util.function.Function;
import ognl.DefaultClassResolver;
import ognl.DefaultMemberAccess;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.apache.commons.lang3.Validate;

/**
 * Provides accessor methods for session objects.
 *
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public class SessionUtils {

  public static User getActiveUser(final HttpDsl httpSpec, final UserSession userSession) {
    Validate.notNull(httpSpec, "Http spec must not be null.");
    Validate.notNull(userSession, "User session must not be null.");

    var userSupplier = httpSpec.getUserSupplier();
    if (userSupplier != null) {
      return userSupplier.get();
    }

    var userAccessor = httpSpec.getUserAccessor();
    if (userAccessor != null) {
      return userAccessor.apply(userSession);
    }

    if (httpSpec.getAuthUser() != null) {
      return httpSpec.getAuthUser();
    }
    return userSession.getUser();
  }

  public static <T> Function<UserSession, T> session(String sessionKey) {
    Validate.notEmpty(sessionKey, "Session key must not be empty.");
    return session -> session.<T>get(sessionKey)
        .orElseThrow(() -> new SessionKeyNotFoundException(sessionKey, Scope.USER));
  }

  public static <T> Function<UserSession, T> global(String sessionKey) {
    Validate.notEmpty(sessionKey, "Session key must not be empty.");
    return session -> session.getSimulationSession()
        .<T>get(sessionKey)
        .orElseThrow(() -> new SessionKeyNotFoundException(sessionKey, Scope.SIMULATION));
  }

  public static <T> Function<UserSession, T> global(String sessionKey, String expression) {
    Validate.notEmpty(sessionKey, "Session key must not be empty.");
    Validate.notEmpty(expression, "Expression must not be empty.");
    return session -> session.getSimulationSession().<T>get(sessionKey)
        .map(o -> readObject(expression, o))
        .orElseThrow(() -> new SessionKeyNotFoundException(sessionKey, Scope.SIMULATION));
  }

  public static <T> Function<UserSession, T> session(String sessionKey, String expression) {
    Validate.notEmpty(sessionKey, "Session key must not be empty.");
    Validate.notEmpty(expression, "Expression must not be empty.");
    return session -> session.<T>get(sessionKey)
        .map(o -> readObject(expression, o))
        .orElseThrow(() -> new SessionKeyNotFoundException(sessionKey, Scope.USER));
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
