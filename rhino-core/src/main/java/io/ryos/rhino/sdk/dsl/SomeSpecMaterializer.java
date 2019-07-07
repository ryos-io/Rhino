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
import io.ryos.rhino.sdk.reporting.MeasurementImpl;
import io.ryos.rhino.sdk.reporting.UserEvent;
import io.ryos.rhino.sdk.reporting.UserEvent.EventType;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import io.ryos.rhino.sdk.specs.SomeSpec;
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
        .flatMap(s -> Mono.fromCallable(() -> {
          var userId = userSession.getUser().getId();
          var measurement = new MeasurementImpl(spec.getTestName(), userId);
          var start = System.currentTimeMillis();
          var userEventStart = new UserEvent();
          userEventStart.elapsed = 0;
          userEventStart.start = start;
          userEventStart.end = start;
          userEventStart.scenario = spec.getTestName();
          userEventStart.eventType = EventType.START;
          userEventStart.id = s.getUser().getId();

          measurement.record(userEventStart);

          var resultingSession = spec.getFunction().apply(s, measurement);
          var elapsed = System.currentTimeMillis() - start;
          var userEventEnd = new UserEvent();
          userEventEnd.elapsed = elapsed;
          userEventEnd.start = start;
          userEventEnd.end = start + elapsed;
          userEventEnd.scenario = spec.getTestName();
          userEventEnd.eventType = EventType.END;
          userEventEnd.id = userId;
          measurement.record(userEventEnd);
          dispatcher.dispatchEvents(measurement);

          return resultingSession;
        }));
  }
}