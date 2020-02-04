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
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.impl.GaugeDslImpl;
import io.ryos.rhino.sdk.reporting.MeasurementImpl;
import io.ryos.rhino.sdk.reporting.UserEvent;
import io.ryos.rhino.sdk.reporting.UserEvent.EventType;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 *
 */
public class MeasureDslMaterializer implements DslMaterializer {

  private final GaugeDslImpl dslItem;

  public MeasureDslMaterializer(GaugeDslImpl dslItem) {
    this.dslItem = dslItem;
  }

  @Override
  public Mono<UserSession> materialize(UserSession userSession) {
    UUID uuid = UUID.randomUUID();
    dslItem.setName(dslItem.getTag());

    return Mono.just(userSession)
        .flatMap(session -> Mono.fromCallable(() -> {
          var start = System.currentTimeMillis();
          userSession.add("measurement-" + uuid + "-start", start);
          return userSession;
        }))
        .flatMap(session -> {
          MaterializableDslItem materializableDslItem = dslItem.getChildren().get(0);
          materializableDslItem.setParent(dslItem);
          return materializableDslItem.materializer().materialize(userSession);
        })
        .flatMap(session -> Mono.fromCallable(() -> {
          var start = session.<Long>get("measurement-" + uuid + "-start").get();
          var userId = userSession.getUser().getId();
          var measurement = new MeasurementImpl(dslItem.getTag(), userId);
          var userEventStart = new UserEvent(
              session.getUser().getUsername(),
              session.getUser().getId(),
              dslItem.getTag(),
              start,
              start,
              0,
              EventType.START,
              null,
              session.getUser().getId()
          );

          measurement.record(userEventStart);

          measurement.measure(dslItem.getName(), " ");
          var elapsed = System.currentTimeMillis() - start;
          var userEventEnd = new UserEvent(
              session.getUser().getUsername(),
              session.getUser().getId(),
              dslItem.getTag(),
              start,
              start + elapsed,
              elapsed,
              EventType.END,
              null,
              session.getUser().getId()
          );

          measurement.record(userEventEnd);

          EventDispatcher.getInstance().dispatchEvents(measurement);

          return session;
        }));
  }
}
