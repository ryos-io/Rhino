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

package io.ryos.rhino.sdk.dsl.mat;

import static io.ryos.rhino.sdk.dsl.specs.builder.SessionAccessor.getActiveUser;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.ResultHandler;
import io.ryos.rhino.sdk.dsl.specs.ForEachSpec;
import io.ryos.rhino.sdk.dsl.specs.HttpResponse;
import io.ryos.rhino.sdk.dsl.specs.HttpSpec;
import io.ryos.rhino.sdk.dsl.specs.Spec.Scope;
import java.util.ArrayList;
import java.util.List;

public class CollectingHttpResultHandler implements ResultHandler<HttpResponse> {

  private final String contextKey;
  private final UserSession userSession;
  private final HttpSpec httpSpec;

  public CollectingHttpResultHandler(final String contextKey,
      final UserSession userSession,
      final HttpSpec httpSpec) {
    this.contextKey = contextKey;
    this.userSession = userSession;
    this.httpSpec = httpSpec;
  }

  public CollectingHttpResultHandler(final UserSession userSession, final HttpSpec httpSpec) {
    this(httpSpec.getResponseKey(), userSession, httpSpec);
  }

  @Override
  public UserSession handle(final HttpResponse resultObject) {
    if (contextKey == null) {
      return userSession;
    }

    var activatedUser = getActiveUser(httpSpec, userSession);
    var key = getKey();
    var result = getResult(resultObject, key);

    if (isInUserSession()) {
      userSession.add(key, result);
    } else {
      var measurementPoint = httpSpec.getMeasurementPoint();
      var specData = userSession.findSimulationSession(activatedUser).<HttpSpecData>get(
          measurementPoint).orElse(new HttpSpecData());
      specData.setResponse(resultObject);
      userSession.findSimulationSession(activatedUser).add(measurementPoint, specData);
    }
    return userSession;
  }

  private boolean isInUserSession() {
    return httpSpec.getSessionScope().equals(Scope.USER);
  }

  private Object getResult(HttpResponse resultObject, String key) {
    var parentSpec = httpSpec.getParentSpec();
    Object result;
    if (parentSpec instanceof ForEachSpec) {
      var resultObjs = userSession.<List<HttpResponse>>get(key).orElse(new ArrayList<>());
      resultObjs.add(resultObject);
      result = resultObjs;
    } else {
      result = resultObject;
    }
    return result;
  }

  private String getKey() {
    var parentSpec = httpSpec.getParentSpec();
    if (parentSpec instanceof ForEachSpec) {
      return ((ForEachSpec) parentSpec).getContextKey();
    }
    return contextKey;
  }
}
