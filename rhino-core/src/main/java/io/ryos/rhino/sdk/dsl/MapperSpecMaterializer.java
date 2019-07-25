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
import io.ryos.rhino.sdk.specs.MapperSpec;
import java.util.function.Function;
import reactor.core.publisher.Mono;

/**
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class MapperSpecMaterializer implements SpecMaterializer<MapperSpec, UserSession> {

  @Override
  public Mono<UserSession> materialize(MapperSpec spec, UserSession userSession) {
    return Mono.just(userSession).map(session -> {
      try {
        var mapper = spec.getMapper();
        var value = userSession
            .get(mapper.getKey())
            .map((Function<Object, Object>) mapper.getMappingFunction()::apply)
            .orElse(null);
        return session.add(mapper.getSaveTo(), value);
      } catch (Exception e) {

        System.out.println(e);
      }
      return userSession;
    });
  }
}