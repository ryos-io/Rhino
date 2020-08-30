package io.ryos.rhino.sdk.dsl.mat;

import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.getActiveUser;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.ForEachDsl;
import io.ryos.rhino.sdk.dsl.SessionDslItem.Scope;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CollectingMaterializer<S, R extends Iterable<S>> implements DslMaterializer {
  private static final Logger LOG = LoggerFactory.getLogger(ForEachDslMaterializer.class);

  private final ForEachDsl<S, R> dslItem;

  public CollectingMaterializer(ForEachDsl<S, R> dslItem) {
    this.dslItem = dslItem;
  }

  @Override
  public Mono<UserSession> materialize(final UserSession session) {

    var iterable = Optional.ofNullable(dslItem.getIterableSupplier().apply(session))
        .orElseThrow(() -> new IllegalArgumentException("forEach() failed."));

    return Flux.fromIterable(iterable)
        .map(dslItem.getMapper())
        .map(object -> collect(session, object, dslItem.getSessionKey(), dslItem.getSessionScope()))
        .reduce((s1, s2) -> s1)
        .doOnError(e -> LOG.error("Unexpected error: ", e));
  }

  public UserSession collect(UserSession userSession, Object response, String sessionKey,
      Scope sessionScope) {

    List<Object> listOfObjects = new CopyOnWriteArrayList<>();
    if (sessionScope.equals(Scope.USER)) {
      listOfObjects = userSession.<List<Object>>get(sessionKey)
          .orElse(new CopyOnWriteArrayList<>());
      listOfObjects.add(response);
      userSession.add(sessionKey, listOfObjects);
    } else {
      var activatedUser = getActiveUser(userSession);
      var globalSession = userSession.getSimulationSessionFor(activatedUser);
      listOfObjects = globalSession.<List<Object>>get(sessionKey)
          .orElse(new CopyOnWriteArrayList<>());
      listOfObjects.add(response);
      globalSession.add(sessionKey, listOfObjects);
    }
    return userSession;
  }
}
