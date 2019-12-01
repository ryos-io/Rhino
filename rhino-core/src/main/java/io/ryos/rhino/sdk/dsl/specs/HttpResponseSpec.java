package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.dsl.specs.SessionDSLItem.Scope;

/**
 * Terminating specification used to store the response object.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface HttpResponseSpec extends DSLSpec {

  HttpSpec saveTo(String sessionKey);

  HttpSpec saveTo(String sessionKey, Scope scope);
}
