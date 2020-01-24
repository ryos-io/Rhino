package io.ryos.rhino.sdk.dsl.mat;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.ForEachDsl;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CollectingMaterializer<S, R extends Iterable<S>> implements
    DslMaterializer<ForEachDsl<S, R>> {

  private static final Logger LOG = LoggerFactory.getLogger(ForEachDslMaterializer.class);

  @Override
  public Mono<UserSession> materialize(final ForEachDsl<S, R> dslItem,
      final UserSession session) {
    var iterable = Optional.ofNullable(dslItem.getIterableSupplier().apply(session))
        .orElseThrow(() -> new IllegalArgumentException("forEach() failed."));

    return Flux.fromIterable(iterable)
        .map(dslItem.getMapper())
        .map(object -> dslItem.handleResult(session, object))
        .reduce((s1, s2) -> s1)
        .doOnError(e -> LOG.error("Unexpected error: ", e));
  }
}
