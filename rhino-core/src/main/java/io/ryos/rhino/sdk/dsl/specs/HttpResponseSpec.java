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

  HttpSpec saveTo(String keyName);

  HttpSpec saveTo(String keyName, Scope scope);
}
