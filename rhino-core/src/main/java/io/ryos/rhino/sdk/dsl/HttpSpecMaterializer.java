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

package io.ryos.rhino.sdk.dsl;

import static org.asynchttpclient.Dsl.delete;
import static org.asynchttpclient.Dsl.get;
import static org.asynchttpclient.Dsl.head;
import static org.asynchttpclient.Dsl.options;
import static org.asynchttpclient.Dsl.put;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.exceptions.RetryableOperationException;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import io.ryos.rhino.sdk.specs.HttpResponse;
import io.ryos.rhino.sdk.specs.HttpSpec;
import io.ryos.rhino.sdk.specs.HttpSpecAsyncHandler;
import io.ryos.rhino.sdk.specs.HttpSpecImpl.RetryInfo;
import io.ryos.rhino.sdk.users.data.OAuthUser;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
  private static final String HEADER_AUTHORIZATION = "Authorization";

  private final AsyncHttpClient client;
  private final EventDispatcher eventDispatcher;
  private final Predicate<UserSession> conditionalSpec;

  public HttpSpecMaterializer(final AsyncHttpClient client,
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

    var httpSpecAsyncHandler = new HttpSpecAsyncHandler(userSession.getUser().getId(),
        spec.getTestName(),
        spec.getMeasurementPoint(), eventDispatcher);

    var responseMono = Mono
        .fromFuture(client.executeRequest(buildRequest(spec, userSession), httpSpecAsyncHandler)
            .toCompletableFuture());

    var retriableMono = Optional.ofNullable(spec.getRetryInfo())
        .map(retryInfo ->
            responseMono
                .map(HttpResponse::new)
                .map(hr -> isRetriable(retryInfo, hr))
                .retryWhen(companion -> companion.zipWith(Flux.range(1, retryInfo.getNumOfRetries()), (error, index) -> {
                    if (index < retryInfo.getNumOfRetries() && error instanceof RetryableOperationException) {
                      return index;
                    } else {
                      throw Exceptions.propagate(error);
                    }
            })))
        .orElse(responseMono);

    return retriableMono.map(response -> (UserSession) userSession.add(spec.getResponseKey(),
        response))
        .onErrorResume(e -> Mono.empty())
        .doOnError(t -> LOG.error("Http Client Error", t.getMessage()));
  }

  private Response isRetriable(final RetryInfo retryInfo, final HttpResponse hr) {
    if (retryInfo.getPredicate().test(hr)) { throw new RetryableOperationException(String.valueOf(hr.getStatusCode())); }
    return hr.getResponse();
  }

  private RequestBuilder buildRequest(HttpSpec httpSpec, UserSession userSession) {

    RequestBuilder builder = null;
    switch (httpSpec.getMethod()) {
      case GET:
        builder = get(httpSpec.getEndpoint().apply(userSession));
        break;
      case HEAD:
        builder = head(httpSpec.getEndpoint().apply(userSession));
        break;
      case OPTIONS:
        builder = options(httpSpec.getEndpoint().apply(userSession));
        break;
      case DELETE:
        builder = delete(httpSpec.getEndpoint().apply(userSession));
        break;
      case PUT:
        builder = put(httpSpec.getEndpoint().apply(userSession))
            .setBody(httpSpec.getUploadContent().get());
        break;
      case POST:
        builder = put(httpSpec.getEndpoint().apply(userSession))
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
      builder = builder.addQueryParam(paramEntry.getKey(), paramEntry.getValue().stream().collect(
          Collectors.joining(",")));
    }

    if (httpSpec.isAuth()) {
      var user = userSession.getUser();
      if (user instanceof OAuthUser) {
        var token = ((OAuthUser) user).getAccessToken();
        builder = builder.addHeader(HEADER_AUTHORIZATION, "Bearer " + token);
      }
    }

    return builder;
  }
}
