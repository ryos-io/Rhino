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

package io.ryos.rhino.sdk.runners;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.ConnectableDsl;
import io.ryos.rhino.sdk.dsl.HttpSpecMaterializer;
import io.ryos.rhino.sdk.dsl.SomeSpecMaterializer;
import io.ryos.rhino.sdk.dsl.WaitSpecMaterializer;
import io.ryos.rhino.sdk.exceptions.MaterializerNotFound;
import io.ryos.rhino.sdk.specs.ConditionalSpecWrapper;
import io.ryos.rhino.sdk.specs.HttpSpec;
import io.ryos.rhino.sdk.specs.SomeSpec;
import io.ryos.rhino.sdk.specs.Spec;
import io.ryos.rhino.sdk.specs.WaitSpec;
import java.util.concurrent.atomic.AtomicInteger;
import org.asynchttpclient.AsyncHttpClient;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class SpecSubscriber extends BaseSubscriber<Tuple2<UserSession, ConnectableDsl>> {

  private final EventDispatcher eventDispatcher;
  private final AsyncHttpClient client;
  private final AtomicInteger clientCapacity =
      new AtomicInteger(0);

  public SpecSubscriber(EventDispatcher eventDispatcher, AsyncHttpClient client) {
    this.eventDispatcher = eventDispatcher;
    this.client = client;
  }

  @Override
  public void hookOnSubscribe(Subscription subscription) {
    request(SimulationConfig.getMaxConnections());
  }

  @Override
  public void hookOnNext(Tuple2<UserSession, ConnectableDsl> tuple) {
    var session = tuple.getT1();
    var dsl = tuple.getT2();
    var specIt = dsl.getSpecs().iterator();
    if (!specIt.hasNext()) {
      throw new RuntimeException("No spec found in DSL.");
    }
    var acc = materialize(specIt.next(), client, session);
    while (specIt.hasNext()) {
      // Never move the following statement into lambda body. next() call is required to be eager.
      var next = specIt.next();
      acc = acc.flatMap(s -> {
        if (next instanceof ConditionalSpecWrapper) {
          var predicate = ((ConditionalSpecWrapper) next).getPredicate();
          if (!predicate.test(s)) {
            return Mono.just(s);
          }
        }
        return materialize(next, client, session);
      });
    }

    acc.subscribe();
  }

  private Mono<UserSession> materialize(final Spec spec,
      final AsyncHttpClient client,
      final UserSession session) {

    if (spec instanceof HttpSpec) {
      return new HttpSpecMaterializer(client, eventDispatcher, null, this)
          .materialize((HttpSpec) spec, session);
    } else if (spec instanceof SomeSpec) {
      return new SomeSpecMaterializer(eventDispatcher).materialize((SomeSpec) spec, session);
    } else if (spec instanceof WaitSpec) {
      return new WaitSpecMaterializer().materialize((WaitSpec) spec, session);
    } else if (spec instanceof ConditionalSpecWrapper) {
      return materialize(((ConditionalSpecWrapper) spec).getSpec(), client, session);
    }

    throw new MaterializerNotFound("Materializer not found for spec: " + spec.getClass().getName());
  }

  public void increase() {
    int i = clientCapacity.incrementAndGet();
    System.out.println(i);
  }

  public void requestAndDecrease() {
    clientCapacity.accumulateAndGet(-1, (capacity, x) -> {
      if (capacity > 0) {
        request(-1);
      }
      return capacity + x;
    });
  }
}
