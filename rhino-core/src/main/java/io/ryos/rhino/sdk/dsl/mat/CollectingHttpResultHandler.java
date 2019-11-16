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

import static io.ryos.rhino.sdk.dsl.specs.SessionDSLItem.Scope.USER;
import static io.ryos.rhino.sdk.dsl.specs.builder.SessionAccessor.getActiveUser;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.ResultHandler;
import io.ryos.rhino.sdk.dsl.specs.DSLItem;
import io.ryos.rhino.sdk.dsl.specs.ForEachSpec;
import io.ryos.rhino.sdk.dsl.specs.HttpResponse;
import io.ryos.rhino.sdk.dsl.specs.HttpSpec;
import io.ryos.rhino.sdk.dsl.specs.ResultingSpec;
import io.ryos.rhino.sdk.dsl.specs.SessionDSLItem;
import io.ryos.rhino.sdk.dsl.specs.impl.AbstractMeasurableSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectingHttpResultHandler implements ResultHandler<HttpResponse> {

  private final String contextKey;
  private final UserSession userSession;
  private final ResultingSpec resultingSpec;

  public CollectingHttpResultHandler(final String contextKey,
      final UserSession userSession,
      final ResultingSpec resultingSpec) {
    this.contextKey = contextKey;
    this.userSession = userSession;
    this.resultingSpec = resultingSpec;
  }

  public CollectingHttpResultHandler(final UserSession userSession, final HttpSpec resultingSpec) {
    this(resultingSpec.getSaveTo(), userSession, resultingSpec);
  }

  @Override
  public UserSession handle(final HttpResponse resultObject) {
    if (contextKey == null) {
      return userSession;
    }

    var activatedUser = getActiveUser((HttpSpec) resultingSpec, userSession);

    if (!resultingSpec.hasParent()) {
      if (isInUserSession()) {
        var httpSpecData = new HttpSpecData();
        httpSpecData.setResponse(resultObject);
        userSession.add(getSessionKey(resultingSpec), httpSpecData);
      } else {
        var globalSession = userSession.getSimulationSessionFor(activatedUser);
        var specData = globalSession.<HttpSpecData>get(getSessionKey(resultingSpec))
            .orElse(new HttpSpecData());
        specData.setResponse(resultObject);
        globalSession.add(getSessionKey(resultingSpec), specData);
      }
    } else {
      if (isInUserSession()) {
        if (resultingSpec.getParent() instanceof ForEachSpec) {
          var stringMap = userSession.<Map<String, List<Object>>>get(
              getSessionKey(resultingSpec.getParent()))
              .orElse(getMapSupplier());
          var httpSpecData = new HttpSpecData();
          httpSpecData.setResponse(resultObject);
          stringMap.get(getSessionKey(resultingSpec)).add(httpSpecData);
          userSession.add(getSessionKey(resultingSpec), stringMap);
        } else {
          var httpSpecData = new HttpSpecData();
          httpSpecData.setResponse(resultObject);
          userSession.add(getSessionKey(resultingSpec.getParent()), httpSpecData);
        }
      } else {

        if (resultingSpec.getParent() instanceof ForEachSpec) {
          var globalSession = userSession.getSimulationSessionFor(activatedUser);
          var stringMap = globalSession.<Map<String, List<Object>>>get(
              getSessionKey(resultingSpec.getParent())).orElse(getMapSupplier());
          var httpSpecData = new HttpSpecData();
          httpSpecData.setResponse(resultObject);
          stringMap.get(getSessionKey(resultingSpec)).add(httpSpecData);
          globalSession.add(getSessionKey(resultingSpec.getParent()), stringMap);
        } else {
          var globalSession = userSession.getSimulationSessionFor(activatedUser);
          var specData = globalSession.<HttpSpecData>get(getSessionKey(resultingSpec.getParent()))
              .orElse(new HttpSpecData());
          specData.setResponse(resultObject);
          globalSession.add(getSessionKey(resultingSpec), specData);
        }
      }
    }

    return userSession;
  }

  private String getSessionKey(final DSLItem spec) {
    if (spec instanceof ResultingSpec) {
      ((ResultingSpec) spec).getSaveTo();
    }

    if (spec instanceof AbstractMeasurableSpec) {
      return ((AbstractMeasurableSpec) spec).getMeasurementPoint();
    }

    throw new RuntimeException("Cannot determine session key.");
  }

  private Map<String, List<Object>> getMapSupplier() {

    var map = new HashMap<String, List<Object>>();
    map.put(getSessionKey(resultingSpec), new ArrayList<>());
    return map;

  }

  private boolean isInUserSession() {

    if (resultingSpec.hasParent() && resultingSpec.getParent() instanceof SessionDSLItem) {
      return ((SessionDSLItem) resultingSpec.getParent()).getSessionScope().equals(USER);
    }

    if (resultingSpec instanceof SessionDSLItem) {
      return ((SessionDSLItem) resultingSpec).getSessionScope().equals(USER);
    }

    throw new RuntimeException("Cannot determine session scope.");
  }
}
