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
import static org.asynchttpclient.Dsl.post;
import static org.asynchttpclient.Dsl.put;

import io.ryos.rhino.sdk.HttpClient;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.dsl.data.HttpResponse;
import io.ryos.rhino.sdk.dsl.data.HttpSpecAsyncHandler;
import io.ryos.rhino.sdk.dsl.impl.HttpDslImpl.RetryInfo;
import io.ryos.rhino.sdk.dsl.utils.SessionUtils;
import io.ryos.rhino.sdk.exceptions.RetryFailedException;
import io.ryos.rhino.sdk.exceptions.RetryableOperationException;
import io.ryos.rhino.sdk.runners.Rampup;
import io.ryos.rhino.sdk.users.BasicAuthRequestStrategy;
import io.ryos.rhino.sdk.users.OAuth2RequestStrategy;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.oauth.OAuthUserImpl;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
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
public class HttpDslMaterializer implements DslMaterializer {

  private static final Logger LOG = LogManager.getLogger(HttpDslMaterializer.class);
  private static final Rampup rampUp = Rampup.getInstance();
  private final HttpDsl dslItem;

  public HttpDslMaterializer(HttpDsl dslItem) {
    this.dslItem = dslItem;
  }

  public Mono<UserSession> materialize(final UserSession userSession) {

    var httpSpecAsyncHandler = new HttpSpecAsyncHandler(userSession, dslItem);

    var sessionMono = Mono.just(userSession);
    if (SimulationConfig.isRampupDefined()) {
      sessionMono = rampUp.rampUp(userSession);
    }

    var responseMono = sessionMono.flatMap(session -> Mono
        .fromFuture(HttpClient.INSTANCE.getClient().executeRequest(buildHttpRequest(
            dslItem, session), httpSpecAsyncHandler).toCompletableFuture()));

    RetryInfo retryInfo = dslItem.getRetryInfo();
    var retriableMono = responseMono;
    if (retryInfo != null) {
      retriableMono = responseMono.map(HttpResponse::new)
          .map(hr -> isRequestRetriable(retryInfo, hr))
          .retryWhen(companion -> companion.zipWith(
              Flux.range(1, retryInfo.getNumOfRetries() + 1), (error, index) -> {
                if (index < retryInfo.getNumOfRetries() + 1
                    && error instanceof RetryableOperationException) {
                  return index;
                } else {
                  throw Exceptions.propagate(new RetryFailedException(error));
                }
              }));
    }

    return retriableMono
        .map(result -> dslItem.handleResult(userSession, new HttpResponse(result)))
        .onErrorResume(handleOnErrorResume(dslItem, httpSpecAsyncHandler))
        .doOnError(t -> LOG.error("Http Client Error", t));
  }

  private Function<Throwable, Mono<? extends UserSession>> handleOnErrorResume(
      final HttpDsl spec, final HttpSpecAsyncHandler httpSpecAsyncHandler) {
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
        builder = put(endpoint);
        if (httpSpec.getUploadContent() != null || httpSpec.getLazyStringPayload() != null) {
          builder.setBody(Optional.ofNullable(httpSpec.getUploadContent()).map(Supplier::get)
              .orElseGet(() -> httpSpec.getLazyStringPayload().apply(userSession)));
        }
        break;
      case POST:
        builder = post(endpoint);
        if (httpSpec.getUploadContent() != null || httpSpec.getLazyStringPayload() != null) {
          builder.setBody(Optional.ofNullable(httpSpec.getUploadContent()).map(Supplier::get)
              .orElseGet(() -> httpSpec.getLazyStringPayload().apply(userSession)));
        }

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
      var specOwner = SessionUtils.getEffectiveHttpUser(httpSpec, userSession);
      builder = handleAuth(specOwner, builder);
    }

    if (SimulationConfig.debugHttp()) {
      LOG.info("[debug.http=true][url={}][headers={}]",
          builder.build().getUrl(),
          builder.build().getHeaders());
    }
    return builder;
  }

  private RequestBuilder handleAuth(User user, RequestBuilder builder) {

    if (user instanceof OAuthUserImpl) {
      return new OAuth2RequestStrategy().addAuthHeaders(builder, user);
    }

    return new BasicAuthRequestStrategy().addAuthHeaders(builder, user);

  }
}
