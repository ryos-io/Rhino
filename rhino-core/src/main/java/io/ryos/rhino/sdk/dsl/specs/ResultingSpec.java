package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.ResultHandler;

/**
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface ResultingSpec<T extends Spec, R> {

  UserSession handleResult(UserSession userSession, R response);

  T withResultHandler(ResultHandler<R> resultHandler);
}
