package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.dsl.SessionDslItem.Scope;

/**
 * Terminating specification used to store the response object.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface HttpResponseDsl extends MaterializableDslItem {

  HttpDsl saveTo(String sessionKey);

  HttpDsl saveTo(String sessionKey, Scope scope);
}
