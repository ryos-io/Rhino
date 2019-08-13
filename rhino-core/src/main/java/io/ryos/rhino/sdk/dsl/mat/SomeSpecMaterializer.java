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
import io.ryos.rhino.sdk.dsl.specs.SomeSpec;
import io.ryos.rhino.sdk.reporting.MeasurementImpl;
import io.ryos.rhino.sdk.reporting.UserEvent;
import io.ryos.rhino.sdk.reporting.UserEvent.EventType;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import reactor.core.publisher.Mono;

/**
 * Some spec materializer.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class SomeSpecMaterializer implements SpecMaterializer<SomeSpec, UserSession> {

  private final EventDispatcher dispatcher;

  public SomeSpecMaterializer(final EventDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public Mono<UserSession> materialize(SomeSpec spec, UserSession userSession) {

    return Mono.just(userSession)
        .flatMap(session -> Mono.fromCallable(() -> {
          var userId = userSession.getUser().getId();
          var measurement = new MeasurementImpl(spec.getTestName(), userId);
          var start = System.currentTimeMillis();
          var userEventStart = new UserEvent(
              session.getUser().getUsername(),
              session.getUser().getId(),
              spec.getTestName(),
              start,
              start,
              0,
              EventType.START,
              null,
              session.getUser().getId()
          );

          measurement.record(userEventStart);

          var status = spec.getFunction().apply(session);

          measurement.measure(spec.getMeasurementPoint(), status);
          var elapsed = System.currentTimeMillis() - start;
          var userEventEnd = new UserEvent(
              session.getUser().getUsername(),
              session.getUser().getId(),
              spec.getTestName(),
              start,
              start + elapsed,
              elapsed,
              EventType.END,
              null,
              session.getUser().getId()
          );

          measurement.record(userEventEnd);

          dispatcher.dispatchEvents(measurement);

          return session;
        }));
  }
}
