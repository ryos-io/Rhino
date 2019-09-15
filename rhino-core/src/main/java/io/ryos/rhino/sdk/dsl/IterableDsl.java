package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.Spec;
import io.ryos.rhino.sdk.dsl.specs.impl.ForEachBuilder;
import java.util.function.Predicate;

public interface IterableDsl extends LoadDsl {

  <E, R extends Iterable<E>> RunnableDsl forEach(ForEachBuilder<E, R> forEachBuilder);

  RunnableDsl runUntil(Predicate<UserSession> predicate, Spec spec);
}
