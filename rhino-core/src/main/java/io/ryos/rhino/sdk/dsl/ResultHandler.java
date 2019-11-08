package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;

public interface ResultHandler<E> {

  UserSession handle(E resultObject);
}
