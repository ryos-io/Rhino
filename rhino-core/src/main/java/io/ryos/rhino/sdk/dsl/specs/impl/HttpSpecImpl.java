package io.ryos.rhino.sdk.dsl.specs.impl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.ResultHandler;
import io.ryos.rhino.sdk.dsl.mat.CollectingHttpResultHandler;
import io.ryos.rhino.sdk.dsl.mat.HttpSpecMaterializer;
import io.ryos.rhino.sdk.dsl.mat.SpecMaterializer;
import io.ryos.rhino.sdk.dsl.specs.DSLItem;
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.HttpConfigSpec;
import io.ryos.rhino.sdk.dsl.specs.HttpResponse;
import io.ryos.rhino.sdk.dsl.specs.HttpRetriableSpec;
import io.ryos.rhino.sdk.dsl.specs.HttpSpec;
import io.ryos.rhino.sdk.dsl.specs.MeasurableSpec;
import io.ryos.rhino.sdk.users.data.User;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.lang3.Validate;

/**
 * HTTP spec implementation of {@link DSLSpec}.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class HttpSpecImpl extends AbstractSessionDSLItem implements HttpSpec, HttpConfigSpec,
    HttpRetriableSpec {

  private Supplier<InputStream> toUpload;

  private List<Function<UserSession, Entry<String, List<String>>>> headers = new ArrayList<>();
  private List<Function<UserSession, Entry<String, List<String>>>> queryParams = new ArrayList<>();
  private List<Function<UserSession, Entry<String, List<String>>>> formParams = new ArrayList<>();
  private boolean authEnabled;
  private User authUser;
  private Method httpMethod;
  private Function<UserSession, String> endpoint;
  private Function<UserSession, User> oauthUserAccessor;
  private Supplier<User> userSupplier;
  private RetryInfo retryInfo;
  private HttpResponse response;
  private ResultHandler<HttpResponse> resultHandler;

  /**
   * Creates a new {@link HttpSpecImpl}.
   * <p>
   *
   * @param name The name of the measurement.
   */
  public HttpSpecImpl(String name) {
    super(Validate.notEmpty(name, "Measurement must not be null."));
    setSessionKey(name);
  }

  @Override
  public HttpRetriableSpec get() {
    this.httpMethod = Method.GET;
    return this;
  }

  @Override
  public HttpRetriableSpec head() {
    this.httpMethod = Method.HEAD;
    return this;
  }

  @Override
  public HttpRetriableSpec put() {
    this.httpMethod = Method.PUT;
    return this;
  }

  @Override
  public HttpRetriableSpec patch() {
    this.httpMethod = Method.PATCH;
    return this;
  }

  @Override
  public HttpRetriableSpec post() {
    this.httpMethod = Method.POST;
    return this;
  }

  @Override
  public HttpRetriableSpec delete() {
    this.httpMethod = Method.DELETE;
    return this;
  }

  @Override
  public HttpRetriableSpec options() {
    this.httpMethod = Method.OPTIONS;
    return this;
  }

  @Override
  public HttpConfigSpec endpoint(final String endpoint) {
    Validate.notEmpty(endpoint, "endpoint must not be empty.");
    this.endpoint = r -> endpoint;
    return this;
  }

  @Override
  public HttpConfigSpec endpoint(Function<UserSession, String> endpoint) {
    Validate.notNull(endpoint, "Endpoint must not be null.");
    this.endpoint = endpoint;
    return this;
  }

  @Override
  public HttpConfigSpec endpoint(BiFunction<UserSession, HttpSpec, String> endpoint) {
    Validate.notNull(endpoint, "Endpoint must not be null.");
    this.endpoint = (session) -> endpoint.apply(session, this);
    return this;
  }

  @Override
  public HttpConfigSpec header(String name, List<String> values) {
    Validate.notEmpty(name, "Header name must not be null.");
    this.headers.add(e -> Map.entry(name, values));
    return this;
  }

  @Override
  public HttpConfigSpec header(String name, String value) {
    Validate.notEmpty(name, "Header name must not be null.");
    this.headers.add(e -> Map.entry(name, Collections.singletonList(value)));
    return this;
  }

  @Override
  public HttpConfigSpec header(Function<UserSession, Entry<String, List<String>>> headerFunction) {
    Validate.notNull(headerFunction, "Header function must not be null.");
    this.headers.add(headerFunction);
    return this;
  }

  @Override
  public HttpConfigSpec formParam(String paramName, List<String> values) {
    Validate.notEmpty("Parameter name must not be empty.", paramName);
    this.formParams.add(e -> Map.entry(paramName, values));
    return this;
  }

  @Override
  public HttpConfigSpec formParam(String paramName, String value) {
    Validate.notEmpty("Parameter name must not be empty.", paramName);
    this.formParams.add(e -> Map.entry(paramName, Collections.singletonList(value)));
    return this;
  }

  @Override
  public HttpConfigSpec formParam(
      Function<UserSession, Entry<String, List<String>>> formParamFunction) {
    Validate.notNull(formParamFunction, "Form parameter function must not be null.");
    this.formParams.add(formParamFunction);
    return this;
  }

  @Override
  public HttpConfigSpec auth() {
    this.authEnabled = true;
    return this;
  }

  @Override
  public HttpConfigSpec auth(User user) {
    Validate.notNull(user, "User must not be null.");
    this.authEnabled = true;
    this.authUser = user;
    return this;
  }

  @Override
  public HttpConfigSpec auth(Function<UserSession, User> sessionAccessor) {
    Validate.notNull(sessionAccessor, "Session accessor must not be null.");
    this.oauthUserAccessor = sessionAccessor;
    this.authEnabled = true;
    return this;
  }

  @Override
  public HttpConfigSpec auth(Supplier<User> userSupplier) {
    Validate.notNull(userSupplier, "userSupplier must not be null.");
    this.userSupplier = userSupplier;
    this.authEnabled = true;
    return this;
  }

  @Override
  public HttpConfigSpec queryParam(String queryParamName, List<String> values) {
    Validate.notEmpty(queryParamName, "Query param name must not be null.");
    this.queryParams.add(e -> Map.entry(queryParamName, values));
    return this;
  }

  @Override
  public HttpConfigSpec queryParam(String queryParamName, String value) {
    Validate.notEmpty(queryParamName, "Query param name must not be null.");
    this.queryParams.add(e -> Map.entry(queryParamName, Collections.singletonList(value)));
    return this;
  }

  @Override
  public HttpConfigSpec queryParam(
      Function<UserSession, Entry<String, List<String>>> queryParamFunction) {
    Validate.notNull(queryParamFunction, "Query param function must not be null.");
    this.queryParams.add(queryParamFunction);
    return this;
  }

  @Override
  public HttpConfigSpec upload(final Supplier<InputStream> inputStream) {
    Validate.notNull(inputStream, "Input stream must not be null.");
    this.toUpload = inputStream;
    return this;
  }

  @Override
  public MeasurableSpec retryIf(final Predicate<HttpResponse> predicate, final int numOfRetries) {
    Validate.isTrue(numOfRetries >= 0, "numberOfRetries must be bigger than zero.");
    Validate.notNull(predicate, "predicate must not be null.");
    this.retryInfo = new RetryInfo(predicate, numOfRetries);
    return this;
  }

  @Override
  public HttpSpec saveTo(String sessionKey, Scope scope) {
    Validate.notNull(sessionKey, "Session key must not be null.");
    Validate.notNull(scope, "scope must not be null.");
    setSessionKey(sessionKey);
    setSessionScope(scope);
    return this;
  }

  @Override
  public HttpSpec saveTo(String sessionKey) {
    Validate.notNull(sessionKey, "Session key must not be null.");
    setSessionKey(sessionKey);
    setSessionScope(Scope.USER);
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
  public List<Function<UserSession, Entry<String, List<String>>>> getHeaders() {
    return headers;
  }

  @Override
  public List<Function<UserSession, Entry<String, List<String>>>> getQueryParameters() {
    return queryParams;
  }

  @Override
  public List<Function<UserSession, Entry<String, List<String>>>> getFormParameters() {
    return formParams;
  }

  @Override
  public Function<UserSession, String> getEndpoint() {
    return endpoint;
  }

  @Override
  public boolean isAuth() {
    return authEnabled;
  }

  @Override
  public User getAuthUser() {
    return authUser;
  }

  @Override
  public String getSaveTo() {
    return getSessionKey();
  }

  @Override
  public HttpResponse getResponse() {
    return response;
  }

  @Override
  public void setResponse(HttpResponse response) {
    this.response = response;
  }

  @Override
  public Function<UserSession, User> getUserAccessor() {
    return oauthUserAccessor;
  }

  @Override
  public Supplier<User> getUserSupplier() {
    return userSupplier;
  }

  @Override
  public RetryInfo getRetryInfo() {
    return retryInfo;
  }

  @Override
  public SpecMaterializer<? extends DSLSpec> createMaterializer(final UserSession session) {
    return new HttpSpecMaterializer();
  }

  @Override
  public UserSession handleResult(UserSession userSession, HttpResponse response) {
    return Optional.ofNullable(resultHandler)
        .orElse(new CollectingHttpResultHandler(userSession, this))
        .handle(response);
  }

  @Override
  public HttpSpec withResultHandler(ResultHandler<HttpResponse> resultHandler) {
    Validate.notNull(resultHandler, "Result handler must not be null.");
    this.resultHandler = resultHandler;
    return this;
  }

  @Override
  public List<DSLItem> getChildren() {
    return Collections.emptyList();
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
