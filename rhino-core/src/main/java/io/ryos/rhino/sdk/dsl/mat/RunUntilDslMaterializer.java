package io.ryos.rhino.sdk.dsl.mat;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.RunUntilDsl;
import reactor.core.publisher.Mono;

public class RunUntilDslMaterializer implements DslMaterializer<RunUntilDsl> {

  @Override
  public Mono<UserSession> materialize(RunUntilDsl dslItem, UserSession userSession) {
    return Mono.just(userSession).map(session -> {
      while (!dslItem.getPredicate().test(userSession)) {
        var targetSpec = dslItem.getSpec();
        var materializer = targetSpec.materializer(userSession);
        materializer.materialize(targetSpec, userSession).block();
      }
      return session;
    });
  }
}
