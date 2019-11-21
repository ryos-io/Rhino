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
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.ForEachSpec;
import io.ryos.rhino.sdk.dsl.specs.builder.ForEachBuilder;
import io.ryos.rhino.sdk.dsl.specs.impl.AbstractSessionDSLItem;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public class ForEachSpecMaterializer<S, R extends Iterable<S>> implements
    SpecMaterializer<ForEachSpec<S, R>> {

  private static final Logger LOG = LoggerFactory.getLogger(ForEachSpecMaterializer.class);

  @Override
  public Mono<UserSession> materialize(final ForEachSpec<S, R> forEachSpec,
      final UserSession session) {

    var forEachBuilder = forEachSpec.getForEachBuilder();
    var iterable = Optional.ofNullable(forEachBuilder.getSessionExtractor().apply(session))
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("forEach() failed. The instance with key: %s", forEachBuilder.getKey())));

    return Flux.fromIterable(iterable)
        .map(forEachBuilder.getForEachFunction())
        .map(childSpec -> inheritFrom(forEachBuilder, childSpec))
        .flatMap(spec -> spec.createMaterializer(session).materialize(spec, session))
        .reduce((s1, s2) -> s1)
        .doOnError(e -> LOG.error("Unexpected error: ", e));
  }

  private DSLSpec inheritFrom(ForEachBuilder<S, R> forEachBuilder, DSLSpec spec) {
    if (isSessionDSLItem(spec)) {
      ((AbstractSessionDSLItem) spec).setSessionScope(forEachBuilder.getScope());
    }
    spec.setParent(forEachBuilder.getSpec());
    return spec;
  }

  private boolean isSessionDSLItem(DSLSpec spec) {
    return spec instanceof AbstractSessionDSLItem;
  }
}
