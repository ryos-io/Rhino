package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import java.util.function.Function;

/**
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public interface ForEachDsl<E, R extends Iterable<E>> extends MaterializableDslItem,
    SessionDslItem, ResultingDsl {

  Function<UserSession, R> getIterableSupplier();

  Function<E, MaterializableDslItem> getForEachFunction();
}
