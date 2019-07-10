package io.ryos.rhino.sdk.specs;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.specs.HttpSpecImpl.RetryInfo;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface HttpSpec extends RetriableSpec<HttpSpec, HttpResponse> {

  enum Method {GET, HEAD, PUT, POST, OPTIONS, DELETE, PATCH}

  static Map.Entry<String, List<String>> from(String key, String value) {
    return Map.entry(key, Collections.singletonList(value));
  }

  HttpSpec get();

  HttpSpec head();

  HttpSpec put();

  HttpSpec post();

  HttpSpec delete();

  HttpSpec patch();

  HttpSpec options();

  HttpSpec upload(final Supplier<InputStream> inputStream);

  HttpSpec endpoint(String endpoint);

  HttpSpec endpoint(Function<UserSession, String> endpoint);

  /**
   * Adds a new header into headers.
   * <p>
   *
   * @param headerFunction Function to get the header value.
   * @return {@link HttpSpec} instance with headers initialized.
   */
  HttpSpec header(Function<UserSession, Entry<String, List<String>>> headerFunction);

  HttpSpec header(String key, List<String> values);

  HttpSpec header(String key, String value);

  HttpSpec auth();

  HttpSpec queryParam(Function<UserSession, Entry<String, List<String>>> headerFunction);

  HttpSpec queryParam(String key, List<String> values);

  HttpSpec queryParam(String key, String value);

  HttpSpec retryIf(Predicate<HttpResponse> predicate, int numOfRetries);

  HttpSpec saveTo(String keyName);

  // Getters
  Method getMethod();

  Function<UserSession, String> getEndpoint();

  Supplier<InputStream> getUploadContent();

  List<Function<UserSession, Entry<String, List<String>>>> getHeaders();

  List<Function<UserSession, Entry<String, List<String>>>> getQueryParameters();

  RetryInfo getRetryInfo();

  boolean isAuth();

  String getResponseKey();
}
