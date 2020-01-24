package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import java.util.function.Function;

/**
 * @author Erhan Bagdemir
 */
public interface ForEachDsl<E, R extends Iterable<E>> extends MaterializableDslItem,
    SessionDslItem, ResultingDsl {

  Function<UserSession, R> getIterableSupplier();

  Function<E, MaterializableDslItem> getForEachFunction();

  Function<E, Object> getMapper();
}
