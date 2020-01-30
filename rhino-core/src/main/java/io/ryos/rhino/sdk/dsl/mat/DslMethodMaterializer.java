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
import io.ryos.rhino.sdk.dsl.DslMethod;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.impl.ConditionalDslWrapper;
import io.ryos.rhino.sdk.exceptions.NoSpecDefinedException;
import io.ryos.rhino.sdk.exceptions.TerminateSimulationException;
import reactor.core.publisher.Mono;

public class DslMethodMaterializer implements DslMaterializer<DslMethod> {

  @Override
  public Mono<UserSession> materialize(final DslMethod dslMethod, final UserSession session) {
    var childrenIterator = dslMethod.getChildren().iterator();
    if (!childrenIterator.hasNext()) {
      throw new NoSpecDefinedException(dslMethod.getName());
    }

    var nextChildDsl = childrenIterator.next();
    nextChildDsl.setParent(dslMethod);
    var materializer = nextChildDsl.materializer(session);
    var acc = materializer.materialize(nextChildDsl, session);

    while (childrenIterator.hasNext()) {
      var next = childrenIterator.next();
      next.setParent(dslMethod);
      acc = acc.flatMap(s -> {
        if (isConditionalSpec(next)) {
          var predicate = ((ConditionalDslWrapper) next).getPredicate();
          if (!predicate.test(s)) {
            return Mono.just(s);
          }
        }
        return next.materializer(session).materialize(next, session);
      });
    }

    return acc.onErrorResume(exception -> {
      if (exception instanceof TerminateSimulationException) {
        return Mono.error(exception);
      }
      return Mono.empty();
    });
  }

  private boolean isConditionalSpec(MaterializableDslItem next) {
    return next instanceof ConditionalDslWrapper;
  }
}
