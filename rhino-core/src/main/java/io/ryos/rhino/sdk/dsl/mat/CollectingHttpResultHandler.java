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
import io.ryos.rhino.sdk.dsl.specs.HttpResponse;
import io.ryos.rhino.sdk.dsl.specs.HttpSpec;
import io.ryos.rhino.sdk.dsl.specs.Spec.Scope;

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
    if (httpSpec.getSessionScope().equals(Scope.USER)) {
      userSession.add(contextKey, resultObject);
    } else {
      var measurementPoint = httpSpec.getMeasurementPoint();
      var specData = userSession.findSimulationSession(activatedUser).<HttpSpecData>get(
          measurementPoint).orElse(new HttpSpecData());
      specData.setResponse(resultObject);
      userSession.findSimulationSession(activatedUser).add(measurementPoint, specData);
    }
    return userSession;
  }
}
