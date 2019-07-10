package io.ryos.rhino.sdk.specs;

import java.util.function.Predicate;

/**
 * Http retriable spec.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface HttpRetriableSpec extends HttpResponseSpec {

  Spec retryIf(Predicate<HttpResponse> predicate, int numOfRetries);
}
