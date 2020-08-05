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

import static io.ryos.rhino.sdk.dsl.SessionDslItem.Scope.USER;
import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.getActiveUser;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.DslItem;
import io.ryos.rhino.sdk.dsl.ForEachDsl;
import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.dsl.ResultHandler;
import io.ryos.rhino.sdk.dsl.ResultingDsl;
import io.ryos.rhino.sdk.dsl.SessionDslItem;
import io.ryos.rhino.sdk.dsl.data.HttpResponse;
import io.ryos.rhino.sdk.dsl.impl.AbstractMeasurableDsl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectingHttpResultHandler implements ResultHandler<HttpResponse> {

  private final String contextKey;
  private final UserSession userSession;
  private final ResultingDsl currentSpec;

  public CollectingHttpResultHandler(final String contextKey,
      final UserSession userSession,
      final ResultingDsl resultingDsl) {
    this.contextKey = contextKey;
    this.userSession = userSession;
    this.currentSpec = resultingDsl;
  }

  public CollectingHttpResultHandler(final UserSession userSession, final HttpDsl httpSpec) {
    this(httpSpec.getSessionKey(), userSession, httpSpec);
  }

  @Override
  public UserSession handle(final HttpResponse resultObject) {
    if (contextKey == null) {
      return userSession;
    }

    var activatedUser = getActiveUser(userSession);
    var globalSession = userSession.getSimulationSessionFor(activatedUser);
    var parentKey =
        currentSpec.getParent() instanceof SessionDslItem && currentSpec
            .getParent() instanceof AbstractMeasurableDsl ?
        getSessionKey(currentSpec.getParent()) : "";
    var currentKey = getSessionKey(currentSpec);
    var httpSpecData = new HttpDslData();
    httpSpecData.setEndpoint(((HttpDsl) currentSpec).getEndpoint().apply(userSession));

    if (!currentSpec.hasParent()) {
      if (isInUserSession()) {
        httpSpecData.setResponse(resultObject);
        userSession.add(currentKey, httpSpecData);
      } else {
        var specData = globalSession.<HttpDslData>get(currentKey).orElse(httpSpecData);
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
          var specData = globalSession.<HttpDslData>get(parentKey).orElse(httpSpecData);
          specData.setResponse(resultObject);
          globalSession.add(currentKey, specData);
        }
      }
    }

    return userSession;
  }

  private boolean isForEachSpec() {
    return currentSpec.getParent() instanceof ForEachDsl;
  }

  private String getSessionKey(final DslItem dslItem) {

    if (dslItem instanceof SessionDslItem) {
      return ((SessionDslItem) dslItem).getSessionKey();
    }

    if (dslItem instanceof AbstractMeasurableDsl) {
      return ((AbstractMeasurableDsl) dslItem).getMeasurementPoint();
    }

    throw new RuntimeException("Cannot determine define key.");
  }

  private Map<String, List<Object>> getMapSupplier(DslItem spec) {
    var map = new HashMap<String, List<Object>>();
    map.put(getSessionKey(spec), new ArrayList<>());
    return map;
  }

  private boolean isInUserSession() {

    if (currentSpec.hasParent() && currentSpec.getParent() instanceof SessionDslItem) {
      return ((SessionDslItem) currentSpec.getParent()).getSessionScope().equals(USER);
    }

    if (currentSpec instanceof SessionDslItem) {
      return ((SessionDslItem) currentSpec).getSessionScope().equals(USER);
    }

    throw new RuntimeException("Cannot determine define scope.");
  }
}
