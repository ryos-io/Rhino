package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import java.util.List;
import java.util.function.Function;

/**
 * @author Erhan Bagdemir
 */
public interface ForEachDsl<E, R extends Iterable<E>> extends MaterializableDslItem,
    SessionDslItem {

  Function<UserSession, R> getIterableSupplier();

  List<Function<E, ? extends MaterializableDslItem>> getForEachFunctions();

  Function<E, Object> getMapper();
}
