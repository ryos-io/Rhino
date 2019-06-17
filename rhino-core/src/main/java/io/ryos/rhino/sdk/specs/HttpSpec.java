package io.ryos.rhino.sdk.specs;

import io.ryos.rhino.sdk.data.Context;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

public interface HttpSpec extends Spec {

  enum Method {GET, HEAD, PUT, POST, OPTIONS, DELETE, PATCH}

  HttpSpec get();
  HttpSpec head();
  HttpSpec put();
  HttpSpec post();
  HttpSpec delete();
  HttpSpec patch();
  HttpSpec options();
  HttpSpec upload(InputStream stream);

  HttpSpec endpoint(String endpoint);
  HttpSpec endpoint(Function<Context, String> endpoint);

  /**
   * Adds a new header into headers.
   * <p>
   *
   * @param headerFunction Function to get the header value.
   * @return {@link HttpSpec} instance with headers initialized.
   */
  HttpSpec header(Function<Context, Entry<String, List<String>>> headerFunction);
  HttpSpec header(String key, List<String> values);
  HttpSpec header(String key, String value);

  HttpSpec queryParam(Function<Context, Entry<String, List<String>>> headerFunction);
  HttpSpec queryParam(String key, List<String> values);
  HttpSpec queryParam(String key, String value);

  // Getters
  Method getMethod();
  Function<Context, String> getEndpoint();
  InputStream getUploadContent();
  List<Function<Context, Entry<String, List<String>>>> getHeaders();
  List<Function<Context, Entry<String, List<String>>>> getQueryParameters();
}
