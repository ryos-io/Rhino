package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;

/**
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface ResultingDsl<T extends MaterializableDslItem, R> extends MaterializableDslItem {

  UserSession handleResult(UserSession userSession, R response);

  String getSaveTo();

  T withResultHandler(ResultHandler<R> resultHandler);
}
