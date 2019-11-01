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

import static org.asynchttpclient.Dsl.delete;
import static org.asynchttpclient.Dsl.get;
import static org.asynchttpclient.Dsl.head;
import static org.asynchttpclient.Dsl.options;
import static org.asynchttpclient.Dsl.put;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.HttpResponse;
import io.ryos.rhino.sdk.dsl.specs.HttpSpec;
import io.ryos.rhino.sdk.dsl.specs.HttpSpecAsyncHandler;
import io.ryos.rhino.sdk.dsl.specs.Spec.Scope;
import io.ryos.rhino.sdk.dsl.specs.impl.HttpSpecImpl.RetryInfo;
import io.ryos.rhino.sdk.exceptions.RetryFailedException;
import io.ryos.rhino.sdk.exceptions.RetryableOperationException;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import io.ryos.rhino.sdk.users.BasicAuthRequestStrategy;
import io.ryos.rhino.sdk.users.OAuth2RequestStrategy;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.data.UserImpl;
import io.ryos.rhino.sdk.users.oauth.OAuthService;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spec materializer takes the spec instances and convert them into reactor components, that are to
 * be executed by reactor runtime.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class HttpSpecMaterializer implements SpecMaterializer<HttpSpec, UserSession> {

  private static final Logger LOG = LogManager.getLogger(HttpSpecMaterializer.class);
  private static final String DEFAULT_RESULT = "result";

  private final AsyncHttpClient client;
  private final EventDispatcher eventDispatcher;
  private final Predicate<UserSession> conditionalSpec;

  HttpSpecMaterializer(final AsyncHttpClient client,
      final EventDispatcher eventDispatcher,
      final Predicate<UserSession> predicate) {
    this.client = client;
    this.eventDispatcher = eventDispatcher;
    this.conditionalSpec = predicate;
  }

  /**
   * Specification materializer translates the specifications into reactor implementations.
   * <p>
   *
   * @param client Async HTTP client instance.
   * @param eventDispatcher Event dispatcher instance.
   */
  public HttpSpecMaterializer(final AsyncHttpClient client, final EventDispatcher eventDispatcher) {
    this(client, eventDispatcher, null);
  }

  public Mono<UserSession> materialize(final HttpSpec spec, final UserSession userSession) {

    if (conditionalSpec != null && !conditionalSpec.test(userSession)) {
      return Mono.just(userSession);
    }

    var httpSpecAsyncHandler = new HttpSpecAsyncHandler(userSession, spec, eventDispatcher);
    var responseMono = Mono.just(userSession)
        .flatMap(s -> Mono.fromCompletionStage(
            client.executeRequest(buildRequest(spec, s), httpSpecAsyncHandler)
                .toCompletableFuture()));

    var retriableMono = Optional.ofNullable(spec.getRetryInfo()).map(retryInfo ->
        responseMono.map(HttpResponse::new)
            .map(hr -> isRetriable(retryInfo, hr))
            .retryWhen(companion ->
                companion
                    .zipWith(Flux.range(1, retryInfo.getNumOfRetries() + 1), (error, index) -> {
                      if (index < retryInfo.getNumOfRetries() + 1
                          && error instanceof RetryableOperationException) {
                        return index;
                      } else {
                        throw Exceptions.propagate(new RetryFailedException(error));
                      }
                    })))
        .orElse(responseMono);

    return retriableMono.map(response -> {
      var key = Optional.ofNullable(spec.getResponseKey()).orElse(DEFAULT_RESULT);
      if (spec.getStorageScope() == Scope.USER) {
        userSession.add(key, response);
      } else {
        userSession.getSimulationSession().add(key, response);
      }

      return userSession;
    })
        .onErrorResume(error -> {
          if (error instanceof RetryFailedException && spec.isCumulativeMeasurement()) {
            httpSpecAsyncHandler.completeMeasurement();
          } else {
            LOG.error(error.getMessage());
          }
          return Mono.empty();
        })
        .doOnError(t -> LOG.error("Http Client Error", t));
  }

  private Response isRetriable(final RetryInfo retryInfo, final HttpResponse hr) {
    if (retryInfo.getPredicate().test(hr)) {
      throw new RetryableOperationException(String.valueOf(hr.getStatusCode()));
    }
    return hr.getResponse();
  }

  private RequestBuilder buildRequest(HttpSpec httpSpec, UserSession userSession) {

    HttpSpecData httpSpecData = new HttpSpecData();
    httpSpecData.setEndpoint(httpSpec.getEndpoint().apply(userSession));

    userSession.add(httpSpec.getMeasurementPoint(), httpSpecData);

    RequestBuilder builder = null;
    switch (httpSpec.getMethod()) {
      case GET:
        builder = get(httpSpecData.getEndpoint());
        break;
      case HEAD:
        builder = head(httpSpecData.getEndpoint());
        break;
      case OPTIONS:
        builder = options(httpSpecData.getEndpoint());
        break;
      case DELETE:
        builder = delete(httpSpecData.getEndpoint());
        break;
      case PUT:
        builder = put(httpSpecData.getEndpoint())
            .setBody(httpSpec.getUploadContent().get());
        break;
      case POST:
        builder = put(httpSpecData.getEndpoint())
            .setBody(httpSpec.getUploadContent().get());
        break;
      // case X : rest of methods, we support...
      default:
        throw new NotImplementedException("Not implemented: " + httpSpec.getMethod());
    }

    for (var f : httpSpec.getHeaders()) {
      var headerEntry = f.apply(userSession);
      builder = builder.addHeader(headerEntry.getKey(), headerEntry.getValue());
    }

    for (var f : httpSpec.getQueryParameters()) {
      var paramEntry = f.apply(userSession);
      builder = builder
          .addQueryParam(paramEntry.getKey(), String.join(",", paramEntry.getValue()));
    }

    for (var f : httpSpec.getFormParameters()) {
      var paramEntry = f.apply(userSession);
      builder = builder
          .addFormParam(paramEntry.getKey(), String.join(",", paramEntry.getValue()));
    }

    if (httpSpec.isAuth()) {
      Function<UserSession, User> userAccessor = httpSpec.getUserAccessor();
      User user = null;
      if (userAccessor != null) {
        user = userAccessor.apply(userSession);
      }
      if (httpSpec.getAuthUser() != null) {
        user = httpSpec.getAuthUser();
      }
      if (user == null) {
        user = userSession.getUser();
      }

      builder = handleAuth(user, builder);
    }

    return builder;
  }

  private RequestBuilder handleAuth(User user, RequestBuilder builder) {

    if (user instanceof UserImpl) {
      return new BasicAuthRequestStrategy().addAuthHeaders(builder, user);
    }

    if (user instanceof OAuthService) {
      return new OAuth2RequestStrategy().addAuthHeaders(builder, user);
    }

    return builder;
  }
}
