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
import io.ryos.rhino.sdk.dsl.specs.MaterializableDslItem;
import reactor.core.publisher.Mono;

/**
 * MaterializableDslItem materializer takes the spec instances and convert them into reactive components, that are
 * to be executed by reactive framework in runtime.
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface SpecMaterializer<T extends MaterializableDslItem> {

  /**
   * Materializer takes the spec and convert it into a {@link Mono}.
   *
   * @param spec List of DSL MaterializableDslItem.
   * @return Mono instance.
   */
  Mono<UserSession> materialize(T spec, final UserSession userSession);
}
