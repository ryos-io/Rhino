package io.ryos.rhino.sdk.dsl.mat;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.RunUntilSpec;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import org.asynchttpclient.AsyncHttpClient;
import reactor.core.publisher.Mono;

public class RunUntilSpecMaterializer implements SpecMaterializer<RunUntilSpec, UserSession> {

  private final EventDispatcher eventDispatcher;
  private final AsyncHttpClient asyncHttpClient;

  public RunUntilSpecMaterializer(EventDispatcher eventDispatcher,
      AsyncHttpClient asyncHttpClient) {
    this.eventDispatcher = eventDispatcher;
    this.asyncHttpClient = asyncHttpClient;
  }

  @Override
  public Mono<UserSession> materialize(RunUntilSpec spec, UserSession userSession) {
    MaterializerFactory materializerFactory = new MaterializerFactory(asyncHttpClient,
        eventDispatcher);
    return Mono.just(userSession).map(session -> {
      while (!spec.getPredicate().test(userSession)) {
        materializerFactory.monoFrom(spec.getSpec(), session).block();
      }
      return session;
    });
  }
}
