package io.ryos.rhino.sdk.specs;

import io.ryos.rhino.sdk.data.Context;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Html implementation of {@link Spec}.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class HttpSpecImpl extends AbstractSpec implements HttpSpec {

  private Supplier<InputStream> toUpload;

  private List<Function<Context, Entry<String, List<String>>>> headers = new ArrayList<>();
  private List<Function<Context, Entry<String, List<String>>>> queryParams = new ArrayList<>();
  private boolean auth;

  private Method httpMethod;
  private Function<Context, String> endpoint;
  private RetryInfo retryInfo;

  private String saveTo;

  /**
   * Creates a new {@link HttpSpecImpl}.
   * <p>
   *
   * @param measurement The name of the measurement.
   */
  public HttpSpecImpl(String measurement) {
    super(measurement);
  }

  @Override
  public HttpSpec get() {
    this.httpMethod = Method.GET;
    return this;
  }

  @Override
  public HttpSpec head() {
    this.httpMethod = Method.HEAD;
    return this;
  }

  @Override
  public HttpSpec put() {
    this.httpMethod = Method.PUT;
    return this;
  }

  @Override
  public HttpSpec patch() {
    this.httpMethod = Method.PATCH;
    return this;
  }

  @Override
  public HttpSpec post() {
    this.httpMethod = Method.POST;
    return this;
  }

  @Override
  public HttpSpec delete() {
    this.httpMethod = Method.DELETE;
    return this;
  }

  @Override
  public HttpSpec options() {
    this.httpMethod = Method.OPTIONS;
    return this;
  }

  @Override
  public HttpSpec endpoint(final String endpoint) {
    this.endpoint = r -> endpoint;
    return this;
  }

  @Override
  public HttpSpec endpoint(Function<Context, String> endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  @Override
  public HttpSpec header(String key, List<String> values) {
    this.headers.add(e -> Map.entry(key, values));
    return this;
  }

  @Override
  public HttpSpec header(String key, String value) {
    this.headers.add(e -> Map.entry(key, Collections.singletonList(value)));
    return this;
  }

  @Override
  public HttpSpec header(Function<Context, Entry<String, List<String>>> headerFunction) {
    this.headers.add(headerFunction);
    return this;
  }

  @Override
  public HttpSpec auth() {
    this.auth = true;
    return this;
  }

  @Override
  public HttpSpec queryParam(String key, List<String> values) {
    this.queryParams.add(e -> Map.entry(key, values));
    return this;
  }

  @Override
  public HttpSpec queryParam(String key, String value) {
    this.queryParams.add(e -> Map.entry(key, Collections.singletonList(value)));
    return this;
  }

  @Override
  public HttpSpec queryParam(Function<Context, Entry<String, List<String>>> headerFunction) {
    this.queryParams.add(headerFunction);
    return this;
  }

  @Override
  public HttpSpec upload(final Supplier<InputStream> inputStream) {
    this.toUpload = inputStream;
    return this;
  }

  @Override
  public HttpSpec retryIf(final Predicate<HttpResponse> predicate, final int numOfRetries) {
    this.retryInfo = new RetryInfo(predicate, numOfRetries);
    return this;
  }

  @Override
  public HttpSpec saveTo(String keyName) {
    this.saveTo = keyName;
    return this;
  }

  @Override
  public Supplier<InputStream> getUploadContent() {
    return toUpload;
  }

  @Override
  public Method getMethod() {
    return httpMethod;
  }

  @Override
  public List<Function<Context, Entry<String, List<String>>>> getHeaders() {
    return headers;
  }

  @Override
  public List<Function<Context, Entry<String, List<String>>>> getQueryParameters() {
    return queryParams;
  }

  @Override
  public Function<Context, String> getEndpoint() {
    return endpoint;
  }

  @Override
  public boolean isAuth() {
    return auth;
  }

  @Override
  public String getResponseKey() {
    return saveTo;
  }

  public RetryInfo getRetryInfo() {
    return retryInfo;
  }

  public static class RetryInfo {

    private Predicate<HttpResponse> predicate;
    private int numOfRetries;

    RetryInfo(final Predicate<HttpResponse> predicate, final int numOfRetries) {
      this.predicate = predicate;
      this.numOfRetries = numOfRetries;
    }

    public Predicate<HttpResponse> getPredicate() {
      return predicate;
    }

    public int getNumOfRetries() {
      return numOfRetries;
    }
  }
}
