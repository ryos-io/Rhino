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
import io.ryos.rhino.sdk.dsl.EnsureDsl;
import io.ryos.rhino.sdk.exceptions.TerminateSimulationException;
import reactor.core.publisher.Mono;

/**
 * Ensure spec matarializer implementation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class EnsureDslMaterializer implements DslMaterializer {

  private final EnsureDsl ensureDsl;

  public EnsureDslMaterializer(EnsureDsl ensureDsl) {
    this.ensureDsl = ensureDsl;
  }

  @Override
  public Mono<UserSession> materialize(UserSession userSession) {
    return Mono
        .just(userSession)
        .flatMap(s -> Mono.fromCallable(() -> {
          if (!ensureDsl.getPredicate().test(s)) {
            throw new TerminateSimulationException(ensureDsl.getCause());
          }
          return s;
        }));
  }
}
