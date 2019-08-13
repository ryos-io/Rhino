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
import io.ryos.rhino.sdk.dsl.specs.ForEachSpec;
import io.ryos.rhino.sdk.dsl.specs.Spec;
import io.ryos.rhino.sdk.dsl.specs.impl.ConditionalSpecWrapper;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import java.util.Iterator;
import java.util.function.Function;
import org.asynchttpclient.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public class LoopSpecMaterializer<E, R extends Iterable<E>> implements
    SpecMaterializer<ForEachSpec<E, R>, UserSession> {

  private static final Logger LOG = LoggerFactory.getLogger(LoopSpecMaterializer.class);

  private final EventDispatcher eventDispatcher;
  private final AsyncHttpClient asyncHttpClient;

  public LoopSpecMaterializer(EventDispatcher eventDispatcher,
      AsyncHttpClient asyncHttpClient) {
    this.eventDispatcher = eventDispatcher;
    this.asyncHttpClient = asyncHttpClient;
  }

  @Override
  public Mono<UserSession> materialize(ForEachSpec<E, R> spec, UserSession session) {
    var key = spec.getForEachBuilder().getKey();

    final Iterable<E> iterable = session.get(key)
        .filter(obj -> obj instanceof Iterable)
        .map(obj -> (Iterable<E>) obj)
        .orElseThrow(() -> new IllegalArgumentException("forEach() failed. The instance with key: "
            + "\"" + spec.getForEachBuilder().getKey() + "\" must be iterable, but was " + session
            .get(key)));

    var materializerFactory = new MaterializerFactory(asyncHttpClient, eventDispatcher);
    Function<E, Spec> loopFunction = spec.getForEachBuilder().getForEachFunction();
    Iterator<E> inputIt = iterable.iterator();
    Mono<UserSession> acc = materializerFactory.monoFrom(loopFunction.apply(inputIt.next()),
        session);

    while (inputIt.hasNext()) {
      // Never move the following statement into lambda body. next() call is required to be eager.
      var next = inputIt.next();
      acc = acc.flatMap(s -> {
        if (next instanceof ConditionalSpecWrapper) {
          var predicate = ((ConditionalSpecWrapper) next).getPredicate();
          if (!predicate.test(s)) {
            return Mono.just(s);
          }
        }
        return materializerFactory.monoFrom(loopFunction.apply(next), session);
      });
    }
    acc = acc.doOnError(e -> LOG.error("Unexpected error: ", e));

    return acc;
  }
}
