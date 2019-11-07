package io.ryos.rhino.sdk.dsl.mat;

import io.ryos.rhino.sdk.data.UserSession;

public interface ResultHandler<E> {

  UserSession handle(E resultObject);
}
