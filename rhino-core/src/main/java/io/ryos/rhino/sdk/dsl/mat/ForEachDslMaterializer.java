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
import io.ryos.rhino.sdk.dsl.ForEachDsl;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.SessionDslItem;
import io.ryos.rhino.sdk.exceptions.SessionObjectNotFoundException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ForEachDslMaterializer<S, R extends Iterable<S>> implements DslMaterializer {

  private static final Logger LOG = LoggerFactory.getLogger(ForEachDslMaterializer.class);
  private final ForEachDsl<S, R> dslItem;

  public ForEachDslMaterializer(ForEachDsl<S, R> dslItem) {
    this.dslItem = dslItem;
  }

  @Override
  public Mono<UserSession> materialize(final UserSession session) {
    final R iterable;
    try { //TODO try-catch is just workaround. Find a way to escalate error with exception.
      iterable = Optional.ofNullable(dslItem.getIterableSupplier().apply(session))
          .orElseThrow(() -> new IllegalArgumentException("forEach() failed."));

    } catch (SessionObjectNotFoundException keyNotFound) {
      LOG.error(keyNotFound.getMessage());
      return Mono.error(keyNotFound);
    }

    return Flux.fromIterable(iterable)
        .flatMap(s -> Flux.fromIterable(dslItem.getForEachFunctions()).map(a -> a.apply(s)))
        .map(childDsl -> populateToChildren(dslItem, childDsl))
        .flatMap(childDsl -> childDsl.materializer().materialize(session))
        .reduce((s1, s2) -> s1)
        .doOnError(e -> LOG.error("Unexpected error: ", e));
  }

  private MaterializableDslItem populateToChildren(final ForEachDsl<S, R> forEachDsl,
      final MaterializableDslItem spec) {
    if (isSessionDSLItem(spec)) {
      ((SessionDslItem) spec).setSessionScope(forEachDsl.getSessionScope());
    }
    spec.setParent(forEachDsl);
    return spec;
  }

  private boolean isSessionDSLItem(final MaterializableDslItem spec) {
    return spec instanceof SessionDslItem;
  }
}
