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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    if (!isChildSpec()) {
      if (isInUserSession()) {
        var httpSpecData = new HttpSpecData();
        httpSpecData.setResponse(resultObject);
        userSession.add(httpSpec.getMeasurementPoint(), httpSpecData);
      } else {
        var globalSession = userSession.getSimulationSessionFor(activatedUser);
        var specData = globalSession.<HttpSpecData>get(httpSpec.getMeasurementPoint())
            .orElse(new HttpSpecData());
        specData.setResponse(resultObject);
        globalSession.add(httpSpec.getMeasurementPoint(), specData);
      }
    } else {
      if (isInUserSession()) {
        if (httpSpec.getParentSpec() instanceof ForEachSpec) {
          var stringMap = userSession.<Map<String, List<Object>>>get(
              httpSpec.getParentSpec().getMeasurementPoint())
              .orElse(getMapSupplier());

          var httpSpecData = new HttpSpecData();
          httpSpecData.setResponse(resultObject);
          stringMap.get(httpSpec.getMeasurementPoint()).add(httpSpecData);
          userSession.add(httpSpec.getMeasurementPoint(), stringMap);
        } else {
          var httpSpecData = new HttpSpecData();
          httpSpecData.setResponse(resultObject);
          userSession.add(httpSpec.getParentSpec().getMeasurementPoint(), httpSpecData);
        }
      } else {

        if (httpSpec.getParentSpec() instanceof ForEachSpec) {
          var globalSession = userSession.getSimulationSessionFor(activatedUser);
          var stringMap = globalSession.<Map<String, List<Object>>>get(
              httpSpec.getParentSpec().getMeasurementPoint()).orElse(getMapSupplier());
          var httpSpecData = new HttpSpecData();
          httpSpecData.setResponse(resultObject);
          stringMap.get(httpSpec.getMeasurementPoint()).add(httpSpecData);
          globalSession.add(httpSpec.getParentSpec().getMeasurementPoint(), stringMap);
        } else {
          var globalSession = userSession.getSimulationSessionFor(activatedUser);
          var specData = globalSession.<HttpSpecData>get(
              httpSpec.getParentSpec().getMeasurementPoint()).orElse(new HttpSpecData());
          specData.setResponse(resultObject);
          globalSession.add(httpSpec.getMeasurementPoint(), specData);
        }
      }
    }

    return userSession;
  }

  private Map<String, List<Object>> getMapSupplier() {

    var map = new HashMap<String, List<Object>>();
    map.put(httpSpec.getMeasurementPoint(), new ArrayList<>());
    return map;

  }

  private boolean isChildSpec() {
    return httpSpec.getParentSpec() != null;
  }

  private boolean isInUserSession() {
    if (httpSpec.getParentSpec() != null) {
      return httpSpec.getParentSpec().getSessionScope().equals(Scope.USER);
    }
    return httpSpec.getSessionScope().equals(Scope.USER);
  }

  private Object getResult(HttpResponse resultObject, String key) {
    var parentSpec = httpSpec.getParentSpec();
    if (parentSpec != null) {
      var map = new HashMap<>();
      if (parentSpec instanceof ForEachSpec) {
        var b = userSession.<List<HttpResponse>>get(key).orElse(new ArrayList<>());
        b.add(resultObject);
        map.put(key, b);
      } else {
        map.put(key, resultObject);
      }
      return map;
    }
    return resultObject;
  }

  private String getContainerKey() {
    var parentSpec = httpSpec.getParentSpec();
    if (parentSpec instanceof ForEachSpec) {
      return ((ForEachSpec) parentSpec).getContextKey();
    }
    return null;
  }
}
