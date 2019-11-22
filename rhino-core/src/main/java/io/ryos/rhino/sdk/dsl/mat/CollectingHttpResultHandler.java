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
  private final ResultingSpec currentSpec;

  public CollectingHttpResultHandler(final String contextKey,
      final UserSession userSession,
      final ResultingSpec resultingSpec) {
    this.contextKey = contextKey;
    this.userSession = userSession;
    this.currentSpec = resultingSpec;
  }

  public CollectingHttpResultHandler(final UserSession userSession, final HttpSpec httpSpec) {
    this(httpSpec.getKey(), userSession, httpSpec);
  }

  @Override
  public UserSession handle(final HttpResponse resultObject) {
    if (contextKey == null) {
      return userSession;
    }

    var activatedUser = getActiveUser((HttpSpec) currentSpec, userSession);
    var globalSession = userSession.getSimulationSessionFor(activatedUser);
    var parentKey = currentSpec.getParent() != null ? getSessionKey(currentSpec.getParent()) : "";
    var currentKey = getSessionKey(currentSpec);
    var httpSpecData = new HttpSpecData();
    httpSpecData.setEndpoint(((HttpSpec) currentSpec).getEndpoint().apply(userSession));

    if (!currentSpec.hasParent()) {
      if (isInUserSession()) {
        httpSpecData.setResponse(resultObject);
        userSession.add(currentKey, httpSpecData);
      } else {
        var specData = globalSession.<HttpSpecData>get(currentKey).orElse(httpSpecData);
        specData.setResponse(resultObject);
        globalSession.add(currentKey, specData);
      }
    } else {
      if (isInUserSession()) {
        if (isForEachSpec()) {
          var stringMap = userSession.<Map<String, List<Object>>>get(parentKey)
              .orElse(getMapSupplier(currentSpec));
          httpSpecData.setResponse(resultObject);
          stringMap.get(currentKey).add(httpSpecData);
          userSession.add(currentKey, stringMap);
        } else {
          httpSpecData.setResponse(resultObject);
          userSession.add(parentKey, httpSpecData);
        }
      } else {
        if (isForEachSpec()) {
          var stringMap = globalSession.<Map<String, List<Object>>>get(parentKey)
              .orElse(getMapSupplier(currentSpec));
          httpSpecData.setResponse(resultObject);
          if (stringMap.containsKey(currentKey)) {
            stringMap.get(currentKey).add(httpSpecData);
          } else {
            var list = new ArrayList<>();
            list.add(httpSpecData);
            stringMap.put(currentKey, list);
          }

          globalSession.add(parentKey, stringMap);
        } else {
          var specData = globalSession.<HttpSpecData>get(parentKey).orElse(httpSpecData);
          specData.setResponse(resultObject);
          globalSession.add(currentKey, specData);
        }
      }
    }

    return userSession;
  }

  private boolean isForEachSpec() {
    return currentSpec.getParent() instanceof ForEachSpec;
  }

  private String getSessionKey(final DSLItem dslItem) {

    if (dslItem instanceof SessionDSLItem) {
      return ((SessionDSLItem) dslItem).getKey();
    }

    if (dslItem instanceof AbstractMeasurableSpec) {
      return ((AbstractMeasurableSpec) dslItem).getMeasurementPoint();
    }

    throw new RuntimeException("Cannot determine session key.");
  }

  private Map<String, List<Object>> getMapSupplier(DSLItem spec) {
    var map = new HashMap<String, List<Object>>();
    map.put(getSessionKey(spec), new ArrayList<>());
    return map;
  }

  private boolean isInUserSession() {

    if (currentSpec.hasParent() && currentSpec.getParent() instanceof SessionDSLItem) {
      return ((SessionDSLItem) currentSpec.getParent()).getSessionScope().equals(USER);
    }

    if (currentSpec instanceof SessionDSLItem) {
      return ((SessionDSLItem) currentSpec).getSessionScope().equals(USER);
    }

    throw new RuntimeException("Cannot determine session scope.");
  }
}
