package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.impl.HttpSpecImpl.RetryInfo;
import io.ryos.rhino.sdk.users.data.User;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

public interface HttpSpec extends RetriableSpec<MeasurableSpec, HttpResponse>, MeasurableSpec,
    ResultingSpec<HttpSpec, HttpResponse> {

  enum Method {GET, HEAD, PUT, POST, OPTIONS, DELETE, PATCH}

  static Map.Entry<String, List<String>> from(String key, String value) {
    return Map.entry(key, Collections.singletonList(value));
  }

  // Getters
  Method getMethod();

  Function<UserSession, String> getEndpoint();

  Supplier<InputStream> getUploadContent();

  List<Function<UserSession, Entry<String, List<String>>>> getHeaders();

  List<Function<UserSession, Entry<String, List<String>>>> getQueryParameters();

  List<Function<UserSession, Entry<String, List<String>>>> getFormParameters();

  RetryInfo getRetryInfo();

  boolean isAuth();

  User getAuthUser();

  HttpResponse getResponse();

  void setResponse(HttpResponse response);

  Function<UserSession, User> getUserAccessor();
}
