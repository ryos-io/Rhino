package io.ryos.rhino.sdk.specs;

/**
 * Terminating specification used to store the response object.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface HttpResponseSpec {

  HttpSpec saveTo(String keyName);
}
