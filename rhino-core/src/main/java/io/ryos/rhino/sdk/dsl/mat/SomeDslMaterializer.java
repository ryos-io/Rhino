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
import io.ryos.rhino.sdk.dsl.SomeDsl;
import io.ryos.rhino.sdk.reporting.MeasurementImpl;
import reactor.core.publisher.Mono;

/**
 * Some spec materializer.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class SomeDslMaterializer implements DslMaterializer {

  private final SomeDsl dslItem;

  public SomeDslMaterializer(SomeDsl dslItem) {
    this.dslItem = dslItem;
  }

  @Override
  public Mono<UserSession> materialize(UserSession userSession) {

    return Mono.just(userSession)
        .flatMap(session -> Mono.fromCallable(() -> {
          var userId = userSession.getUser().getId();
          var measurement = new MeasurementImpl(dslItem.getParentName(), userId);
          measurement.start();

          var status = dslItem.getFunction().apply(session);

          measurement.measure(dslItem.getName(), status);
          measurement.finish();
          return session;
        }));
  }
}
