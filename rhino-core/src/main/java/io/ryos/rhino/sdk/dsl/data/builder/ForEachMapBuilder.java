package io.ryos.rhino.sdk.dsl.data.builder;

import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.SessionDslItem.Scope;

public interface ForEachMapBuilder<E, R extends Iterable<E>, T extends MaterializableDslItem> {

  ForEachBuilder<E, R, T> collect(String sessionKey);

  ForEachBuilder<E, R, T> collect(String sessionKey, Scope scope);
}
