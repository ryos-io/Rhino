package io.ryos.rhino.sdk.specs;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.specs.HttpSpecImpl.RetryInfo;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

public interface HttpSpec extends RetriableSpec<MeasurableSpec, HttpResponse>, MeasurableSpec {

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

  RetryInfo getRetryInfo();

  boolean isAuth();

  String getResponseKey();

  Scope getStorageScope();
}
