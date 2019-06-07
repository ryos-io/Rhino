package io.ryos.rhino.sdk.specs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;

public class HttpSpecImpl implements HttpSpec {

  private String enclosingSpec;
  private String stepName;
  private String target;
  private Map<String, String> queryParams = new HashMap<>();
  private Map<String, List<Object>> headers = new HashMap<>();
  private Map<String, String> matrixParams = new HashMap<>();
  private Method httpMethod;

  public HttpSpecImpl(String stepName) {
    this.stepName = stepName;
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
    throw new NotImplementedException("Http PUT()");
  }

  @Override
  public HttpSpec post() {
    throw new NotImplementedException("Http POST()");
  }

  @Override
  public HttpSpec delete() {
    throw new NotImplementedException("Http DELETE()");
  }

  @Override
  public HttpSpec patch() {
    throw new NotImplementedException("Http PATCH()");
  }

  @Override
  public HttpSpec options() {
    throw new NotImplementedException("Http OPTIONS()");
  }

  @Override
  public HttpSpec target(final String endpoint) {
    this.target = endpoint;
    return this;
  }

  @Override
  public HttpSpec headers(final String name, final String... values) {
    this.headers.put(name, Arrays.asList(values));
    return this;
  }

  @Override
  public HttpSpec queryParam(final String name, final String value) {
    this.queryParams.put(name, value);
    return this;
  }

  @Override
  public HttpSpec matrixParams(final String name, final String value) {
    this.matrixParams.put(name, value);
    return this;
  }

  @Override
  public Method getMethod() {
    return httpMethod;
  }

  @Override
  public Map<String, List<Object>> getHeaders() {
    return headers;
  }

  @Override
  public Map<String, String> getQueryParameters() {
    return queryParams;
  }

  @Override
  public Spec withSpecName(final String name) {
    this.enclosingSpec = name;
    return this;
  }

  @Override
  public String getName() {
    return this.stepName;
  }

  @Override
  public String getTarget() {
    return target;
  }

  @Override
  public String getEnclosingSpec() {
    return enclosingSpec;
  }

  @Override
  public String getStepName() {
    return stepName;
  }
}
