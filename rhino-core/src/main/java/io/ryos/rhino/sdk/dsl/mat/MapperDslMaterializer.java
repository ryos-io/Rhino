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
import io.ryos.rhino.sdk.dsl.MapperDsl;
import io.ryos.rhino.sdk.dsl.SessionDslItem.Scope;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

/**
 * Mapper spec materializer implementation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class MapperDslMaterializer implements DslMaterializer {
  private static final Logger LOG = LogManager.getLogger(MapperDslMaterializer.class);

  private final MapperDsl dslItem;

  public MapperDslMaterializer(MapperDsl dslItem) {
    this.dslItem = dslItem;
  }

  @Override
  public Mono<UserSession> materialize(final UserSession userSession) {
    return Mono.just(userSession).map(session -> {
      try {
        var mapperBuilder = dslItem.getMapperBuilder();
        var sessionScope = mapperBuilder.getSessionScope();
        var sessionExtractor = mapperBuilder.getSessionExtractor();
        var sessionValue = Optional.ofNullable(sessionExtractor)
            .map(f -> f.apply(session))
            .orElseGet(() ->
                userSession.get(mapperBuilder.getKey()).orElseThrow(() -> new IllegalArgumentException(
                    "No define object found with the key: " + mapperBuilder.getKey())));

        Object mappedValue;
        if (sessionValue instanceof Iterable) {
          mappedValue = StreamSupport.stream(((Iterable) sessionValue).spliterator(), false)
              .map(o -> mapperBuilder.getMappingFunction().apply(o))
              .collect(Collectors.toList());
        } else {
          mappedValue = mapperBuilder.getMappingFunction().apply(sessionValue);
        }

        if (sessionScope.equals(Scope.SIMULATION)) {
          userSession.getSimulationSession().add(mapperBuilder.getSaveTo(), mappedValue);
        } else {
          session.add(mapperBuilder.getSaveTo(), mappedValue);
        }

        return session;
      } catch (Exception e) {
        LOG.error(e);
      }
      return userSession;
    });
  }
}
