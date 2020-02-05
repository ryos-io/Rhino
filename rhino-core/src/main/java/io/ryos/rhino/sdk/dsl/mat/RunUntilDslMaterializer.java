package io.ryos.rhino.sdk.dsl.mat;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.RunUntilDsl;
import reactor.core.publisher.Mono;

public class RunUntilDslMaterializer implements DslMaterializer {

  private final RunUntilDsl dslItem;

  public RunUntilDslMaterializer(RunUntilDsl dslItem) {
    this.dslItem = dslItem;
  }

  @Override
  public Mono<UserSession> materialize(UserSession userSession) {
    return Mono.just(userSession).map(session -> {
      while (!dslItem.getPredicate().test(userSession)) {
        var targetSpec = dslItem.getSpec();
        var materializer = targetSpec.materializer();
        materializer.materialize(userSession).block();
      }
      return session;
    });
  }
}
