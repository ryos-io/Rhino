package io.ryos.rhino.sdk.dsl.mat;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.RunUntilSpec;
import reactor.core.publisher.Mono;

public class RunUntilSpecMaterializer implements SpecMaterializer<RunUntilSpec> {

  @Override
  public Mono<UserSession> materialize(RunUntilSpec spec, UserSession userSession) {
    return Mono.just(userSession).map(session -> {
      while (!spec.getPredicate().test(userSession)) {
        var targetSpec = spec.getSpec();
        targetSpec.createMaterializer(userSession).materialize(spec, userSession).block();
      }
      return session;
    });
  }
}
