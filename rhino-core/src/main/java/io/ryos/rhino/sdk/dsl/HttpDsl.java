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

package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.data.HttpResponse;
import io.ryos.rhino.sdk.dsl.impl.HttpDslImpl.RetryInfo;
import io.ryos.rhino.sdk.users.data.User;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

public interface HttpDsl extends RetriableDsl<MeasurableDsl, HttpResponse>,
    SessionDslItem,
    MeasurableDsl,
    VerifiableDslItem,
    ResultingDsl<HttpResponse> {

  public enum Method {
    GET, HEAD, PUT, POST, OPTIONS, DELETE, PATCH
  }

  HttpDsl waitResult();

  boolean isWaitResult();

  // Getters
  Method getMethod();

  Function<UserSession, String> getEndpoint();

  Supplier<InputStream> getUploadContent();

  Function<UserSession, String> getLazyStringPayload();

  List<Function<UserSession, Entry<String, List<String>>>> getHeaders();

  List<Function<UserSession, Entry<String, List<String>>>> getQueryParameters();

  List<Function<UserSession, Entry<String, List<String>>>> getFormParameters();

  RetryInfo getRetryInfo();

  boolean isAuth();

  User getAuthUser();

  HttpResponse getResponse();

  void setResponse(HttpResponse response);

  Function<UserSession, User> getUserAccessor();

  Supplier<User> getUserSupplier();
}
