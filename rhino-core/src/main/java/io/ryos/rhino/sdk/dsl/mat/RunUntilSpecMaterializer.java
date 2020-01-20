package io.ryos.rhino.sdk.dsl.mat;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.RunUntilDsl;
import reactor.core.publisher.Mono;

public class RunUntilSpecMaterializer implements SpecMaterializer<RunUntilDsl> {

  @Override
  public Mono<UserSession> materialize(RunUntilDsl spec, UserSession userSession) {
    return Mono.just(userSession).map(session -> {
      while (!spec.getPredicate().test(userSession)) {
        var targetSpec = spec.getSpec();
        var materializer = targetSpec.materializer(userSession);
        materializer.materialize(targetSpec, userSession).block();
      }
      return session;
    });
  }
}
