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

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.ResultHandler;
import io.ryos.rhino.sdk.dsl.specs.EnsureSpec;
import io.ryos.rhino.sdk.dsl.specs.ForEachSpec;
import io.ryos.rhino.sdk.dsl.specs.HttpResponse;
import io.ryos.rhino.sdk.dsl.specs.HttpSpec;
import io.ryos.rhino.sdk.dsl.specs.MapperSpec;
import io.ryos.rhino.sdk.dsl.specs.RunUntilSpec;
import io.ryos.rhino.sdk.dsl.specs.SessionSpec;
import io.ryos.rhino.sdk.dsl.specs.SomeSpec;
import io.ryos.rhino.sdk.dsl.specs.Spec;
import io.ryos.rhino.sdk.dsl.specs.WaitSpec;
import io.ryos.rhino.sdk.dsl.specs.impl.ConditionalSpecWrapper;
import io.ryos.rhino.sdk.exceptions.MaterializerNotFound;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import org.asynchttpclient.AsyncHttpClient;
import reactor.core.publisher.Mono;

/**
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public class MaterializerFactory {

  private final AsyncHttpClient httpClient;
  private final EventDispatcher eventDispatcher;

  public MaterializerFactory(AsyncHttpClient httpClient, EventDispatcher dispatcher) {
    this.httpClient = httpClient;
    this.eventDispatcher = dispatcher;
  }

  public Mono<UserSession> monoFrom(final Spec spec, final UserSession session) {
    return monoFrom(spec, session, null);
  }

  public <R> Mono<UserSession> monoFrom(
      final Spec spec,
      final UserSession session,
      final ResultHandler<R> resultHandler) {

    if (spec instanceof HttpSpec) {
      var handler = (ResultHandler<HttpResponse>) resultHandler;
      if (handler == null) {
        handler = new CollectingHttpResultHandler(session, (HttpSpec) spec);
      }
      var materializer = new HttpSpecMaterializer(httpClient, eventDispatcher, handler);
      return materializer.materialize((HttpSpec) spec, session);
    } else if (spec instanceof SomeSpec) {
      return new SomeSpecMaterializer(eventDispatcher).materialize((SomeSpec) spec, session);
    } else if (spec instanceof WaitSpec) {
      return new WaitSpecMaterializer().materialize((WaitSpec) spec, session);
    } else if (spec instanceof MapperSpec) {
      return new MapperSpecMaterializer().materialize((MapperSpec) spec, session);
    } else if (spec instanceof ForEachSpec) {
      return new LoopSpecMaterializer<>(eventDispatcher, httpClient)
          .materialize((ForEachSpec) spec, session);
    } else if (isConditionalSpec(spec)) {
      return monoFrom(((ConditionalSpecWrapper) spec).getSpec(), session);
    } else if (spec instanceof EnsureSpec) {
      return new EnsureSpecMaterializer().materialize((EnsureSpec) spec, session);
    } else if (spec instanceof RunUntilSpec) {
      return new RunUntilSpecMaterializer(eventDispatcher, httpClient)
          .materialize((RunUntilSpec) spec, session);
    } else if (spec instanceof SessionSpec) {
      return new SessionSpecMaterializer().materialize((SessionSpec) spec, session);
    }

    throw new MaterializerNotFound("Materializer not found for spec: " + spec.getClass().getName());
  }

  private boolean isConditionalSpec(Spec next) {
    return next instanceof ConditionalSpecWrapper;
  }
}
