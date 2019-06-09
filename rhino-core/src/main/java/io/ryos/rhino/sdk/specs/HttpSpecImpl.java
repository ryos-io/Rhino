package io.ryos.rhino.sdk.specs;

import io.ryos.rhino.sdk.data.UserSession;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.asynchttpclient.Response;

/**
 * Html implementation of {@link Spec}.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class HttpSpecImpl extends AbstractSpec implements HttpSpec {

  private InputStream toUpload;
  private String enclosingSpec;
  private String stepName;
  private String target;
  private Map<String, String> queryParams = new HashMap<>();
  private Map<String, List<Object>> headers = new HashMap<>();
  private Map<String, String> matrixParams = new HashMap<>();
  private Method httpMethod;
  private Spec parent;

  private Consumer<Response> afterThenConsumer;
  private Spec afterThenSpec;

  public HttpSpecImpl(String stepName) {
    this.stepName = stepName;
  }

  public HttpSpecImpl(String stepName, Spec parent) {
    this.stepName = stepName;
    this.parent = parent;
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
  public HttpSpec upload(final InputStream inputStream) {
    this.toUpload = inputStream;
    return this;
  }

  @Override
  public InputStream getUploadContent() {
    return toUpload;
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
  public Spec withName(final String name) {
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

  public Consumer<Response> getAfterThenConsumer() {
    return afterThenConsumer;
  }

  public Spec getAfterThenSpec() {
    return afterThenSpec;
  }

  public Spec getParent() {
    return parent;
  }
}
