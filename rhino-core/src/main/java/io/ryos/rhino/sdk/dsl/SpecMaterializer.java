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

import io.ryos.rhino.sdk.Simulation;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.specs.HttpSpec;
import io.ryos.rhino.sdk.specs.HttpSpecAsyncHandler;
import io.ryos.rhino.sdk.users.data.OAuthUser;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import reactor.core.publisher.Mono;

/**
 * Spec materializer takes the spec instances and convert them into reactor components, that are to
 * be executed.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class SpecMaterializer {

  private static final Logger LOG = LogManager.getLogger(SpecMaterializer.class);

  private final AsyncHttpClient client;

  public SpecMaterializer(final AsyncHttpClient client) {
    this.client = client;
  }

  /**
   * Creates an ultimate {@link Mono<Response>} which is the product of all chained {@link
   * Mono<Response>} instances.
   * <p>
   *
   * @param executables Executables returned from the DSL.
   * @return The {@link Mono<Response>} instance.
   */
  public Mono<Response> materialize(List<HttpSpec> executables, UserSession userSession,
      SimulationMetadata simulationMetadata) {

    Iterator<HttpSpec> iterator = executables.iterator();
    if (!iterator.hasNext()) {
      return null;
    }
    var acc = toMono(iterator.next(), null, userSession, simulationMetadata);
    while (iterator.hasNext()) {
      // Never move the following statement into lambda body. next() call is required to be eager.
      HttpSpec next = iterator.next();
      acc = acc.flatMap(response -> toMono(next, response, userSession, simulationMetadata));

    }
    return acc.doOnError((t) -> System.out.println(t.getMessage()));
  }

  private Mono<Response> toMono(final HttpSpec spec, final Response response,
      final UserSession session,
      final SimulationMetadata simulationMetadata) {

    var httpSpecAsyncHandler = new HttpSpecAsyncHandler(session.getUser().getId(),
        spec.getTestName(),
        spec.getMeasurementName(), simulationMetadata);

    return Mono.fromFuture(client.executeRequest(buildRequest(spec, session, response),
        httpSpecAsyncHandler)
        .toCompletableFuture())
        .doOnError((t) -> LOG.error("Http Client Error", t));
  }

  private RequestBuilder buildRequest(HttpSpec httpSpec, UserSession userSession,
      Response response) {

    RequestBuilder builder = null;
    switch (httpSpec.getMethod()) {
      case GET:
        builder = get(httpSpec.getEndpoint().apply(response));
        break;
      case HEAD:
        builder = head(httpSpec.getEndpoint().apply(response));
        break;
      case OPTIONS:
        builder = options(httpSpec.getEndpoint().apply(response));
        break;
      case DELETE:
        builder = delete(httpSpec.getEndpoint().apply(response));
        break;
      case PUT:
        builder = put(httpSpec.getEndpoint().apply(response)).setBody(httpSpec.getUploadContent());
        break;
      case POST:
        builder = put(httpSpec.getEndpoint().apply(response)).setBody(httpSpec.getUploadContent());
        break;
      // case X : rest of methods, we support...
      default:
        throw new NotImplementedException("Not implemented: " + httpSpec.getMethod());
    }

    for (var f : httpSpec.getHeaders()) {
      var headerEntry = f.apply(response);
      builder = builder.addHeader(headerEntry.getKey(), headerEntry.getValue());
    }

    for (var f : httpSpec.getQueryParameters()) {
      var paramEntry = f.apply(response);
      builder = builder.addHeader(paramEntry.getKey(), paramEntry.getValue());
    }

    var user = userSession.getUser();
    if (user instanceof OAuthUser) {
      var token = ((OAuthUser) user).getAccessToken();
      builder = builder.addHeader("Authorization", "Bearer " + token);
    }

    return builder;
  }
}
