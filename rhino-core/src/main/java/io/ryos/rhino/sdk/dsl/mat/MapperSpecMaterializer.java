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
import io.ryos.rhino.sdk.dsl.specs.MapperSpec;
import java.util.function.Function;
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
public class MapperSpecMaterializer implements SpecMaterializer<MapperSpec> {

  private static final Logger LOG = LogManager.getLogger(MapperSpecMaterializer.class);

  @Override
  public Mono<UserSession> materialize(MapperSpec spec, UserSession userSession) {
    return Mono.just(userSession).map(session -> {
      try {
        var mapper = spec.getMapperBuilder();
        var value = userSession
            .get(mapper.getKey())
            .map((Function<Object, Object>) mapper.getMappingFunction())
            .orElse(null);
        return session.add(mapper.getSaveTo(), value);
      } catch (Exception e) {
        LOG.error(e);
      }
      return userSession;
    });
  }
}
