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

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.specs.ConditionalSpecWrapper;
import io.ryos.rhino.sdk.specs.LoopSpec;
import io.ryos.rhino.sdk.specs.Spec;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import org.asynchttpclient.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public class LoopSpecMaterializer<E, R extends Iterable<E>> implements SpecMaterializer<LoopSpec<E, R>, UserSession> {

  private static final Logger LOG = LoggerFactory.getLogger(LoopSpecMaterializer.class);

  private final EventDispatcher eventDispatcher;
  private final AsyncHttpClient asyncHttpClient;

  public LoopSpecMaterializer(EventDispatcher eventDispatcher,
      AsyncHttpClient asyncHttpClient) {
    this.eventDispatcher = eventDispatcher;
    this.asyncHttpClient = asyncHttpClient;
  }

  @Override
  public Mono<UserSession> materialize(LoopSpec<E, R> spec, UserSession session) {
    String key = spec.getLoopBuilder().getKey();
    Optional<Iterable<E>> es = session.get(key);
    if (es.isEmpty()) {
      return Mono.empty();
    }

    Iterable<E> it = es.get();
    var materializerFactory = new MaterializerFactory(asyncHttpClient, eventDispatcher);

    Function<E, Spec> loopFunction = spec.getLoopBuilder().getLoopFunction();
    Iterator<E> inputIt = it.iterator();
    Mono<UserSession> acc = materializerFactory.monoFrom(loopFunction.apply(inputIt.next()), session);

    while (inputIt.hasNext()) {
      // Never move the following statement into lambda body. next() call is required to be eager.
      var next = inputIt.next();
      acc = acc.flatMap(s -> {
        if (next instanceof ConditionalSpecWrapper) {
          var predicate = ((ConditionalSpecWrapper) next).getPredicate();
          if (!predicate.test(s)) { return Mono.just(s); }
        }
        return materializerFactory.monoFrom(loopFunction.apply(next), session);
      });
    }
    acc = acc.doOnError(e -> LOG.error("Unexpected error: ", e));

    return acc;
  }
}
