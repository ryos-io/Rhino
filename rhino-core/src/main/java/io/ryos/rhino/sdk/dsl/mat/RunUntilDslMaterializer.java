package io.ryos.rhino.sdk.dsl.mat;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.RunUntilDsl;
import java.util.Optional;
import reactor.core.publisher.Mono;

public class RunUntilDslMaterializer implements DslMaterializer {

  private final RunUntilDsl dslItem;

  public RunUntilDslMaterializer(RunUntilDsl dslItem) {
    this.dslItem = dslItem;
  }

  @Override
  public Mono<UserSession> materialize(UserSession userSession) {
    return Optional.ofNullable(dslItem.getPredicate())
        .map(p -> Mono.just(userSession)
            .flatMap(session -> dslItem.getSpec().materializer().materialize(userSession))
            .repeat(() -> !dslItem.getPredicate().test(userSession)).last())
        .orElseGet(() -> Mono.just(userSession)
            .flatMap(session -> dslItem.getSpec().materializer().materialize(userSession))
            .repeat(dslItem.getMaxRepeat() - 1)
            .last());
  }
}
