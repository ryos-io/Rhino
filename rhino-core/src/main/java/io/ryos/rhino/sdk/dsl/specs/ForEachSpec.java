package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.data.UserSession;
import java.util.function.Function;

/**
 * @author Erhan Bagdemir
 * @since 1.7.0
 */
public interface ForEachSpec<E, R extends Iterable<E>> extends DSLSpec, SessionDSLItem {

  Function<UserSession, R> getIterableSupplier();

  Function<E, DSLSpec> getForEachFunction();
}
