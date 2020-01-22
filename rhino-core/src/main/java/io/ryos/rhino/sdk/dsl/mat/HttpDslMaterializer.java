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

import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.getActiveUser;
import static org.asynchttpclient.Dsl.delete;
import static org.asynchttpclient.Dsl.get;
import static org.asynchttpclient.Dsl.head;
import static org.asynchttpclient.Dsl.options;
import static org.asynchttpclient.Dsl.put;

import io.ryos.rhino.sdk.HttpClient;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.data.HttpResponse;
import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.dsl.data.HttpSpecAsyncHandler;
import io.ryos.rhino.sdk.dsl.impl.HttpDslImpl.RetryInfo;
import io.ryos.rhino.sdk.exceptions.RetryFailedException;
import io.ryos.rhino.sdk.exceptions.RetryableOperationException;
import io.ryos.rhino.sdk.users.BasicAuthRequestStrategy;
import io.ryos.rhino.sdk.users.OAuth2RequestStrategy;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.data.UserImpl;
import io.ryos.rhino.sdk.users.oauth.OAuthService;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * MaterializableDslItem materializer takes the spec instances and convert them into reactor components, that are to
 * be executed by reactor runtime.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class HttpDslMaterializer implements DslMaterializer<HttpDsl> {
  private static final Logger LOG = LogManager.getLogger(HttpDslMaterializer.class);

  public Mono<UserSession> materialize(final HttpDsl dslItem, final UserSession userSession) {

    var httpSpecAsyncHandler = new HttpSpecAsyncHandler(userSession, dslItem);
    var responseMono = Mono.just(userSession)
        .flatMap(session -> Mono.fromCompletionStage(HttpClient.INSTANCE.getClient().executeRequest(buildHttpRequest(
            dslItem, session), httpSpecAsyncHandler).toCompletableFuture()));
    var retriableMono = Optional.ofNullable(dslItem.getRetryInfo()).map(retryInfo ->
        responseMono.map(HttpResponse::new)
            .map(hr -> isRequestRetriable(retryInfo, hr))
            .retryWhen(companion -> companion.zipWith(
                Flux.range(1, retryInfo.getNumOfRetries() + 1), (error, index) -> {
                  if (index < retryInfo.getNumOfRetries() + 1
                      && error instanceof RetryableOperationException) {
                        return index;
                      } else {
                        throw Exceptions.propagate(new RetryFailedException(error));
                      }
                }))).orElse(responseMono);

    return retriableMono
        .map(result -> dslItem.handleResult(userSession, new HttpResponse(result)))
        .onErrorResume(handleOnErrorResume(dslItem, httpSpecAsyncHandler))
        .doOnError(t -> LOG.error("Http Client Error", t));
  }

  private Function<Throwable, Mono<? extends UserSession>> handleOnErrorResume(final HttpDsl spec,
      final HttpSpecAsyncHandler httpSpecAsyncHandler) {
    return error -> {
      if (error instanceof RetryFailedException && spec.isCumulative()) {
        httpSpecAsyncHandler.completeMeasurement();
      } else {
        LOG.error(error.getMessage(), error);
      }
      return Mono.empty();
    };
  }

  private Response isRequestRetriable(final RetryInfo retryInfo, final HttpResponse httpResponse) {
    if (retryInfo.getPredicate().test(httpResponse)) {
      throw new RetryableOperationException(String.valueOf(httpResponse.getStatusCode()));
    }
    return httpResponse.getResponse();
  }

  private RequestBuilder buildHttpRequest(HttpDsl httpSpec, UserSession userSession) {
    var endpoint = httpSpec.getEndpoint().apply(userSession);

    RequestBuilder builder = null;
    switch (httpSpec.getMethod()) {
      case GET:
        builder = get(endpoint);
        break;
      case HEAD:
        builder = head(endpoint);
        break;
      case OPTIONS:
        builder = options(endpoint);
        break;
      case DELETE:
        builder = delete(endpoint);
        break;
      case PUT:
        builder = put(endpoint)
            .setBody(httpSpec.getUploadContent().get());
        break;
      case POST:
        builder = put(endpoint)
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
      var specOwner = getActiveUser(httpSpec, userSession);
      builder = handleAuth(specOwner, builder);
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
