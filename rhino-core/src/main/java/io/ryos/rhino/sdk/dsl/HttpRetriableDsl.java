package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.dsl.data.HttpResponse;
import java.util.function.Predicate;

/**
 * Http retriable spec.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface HttpRetriableDsl extends HttpResponseDsl {

  MaterializableDslItem retryIf(Predicate<HttpResponse> predicate, int numOfRetries);
}
