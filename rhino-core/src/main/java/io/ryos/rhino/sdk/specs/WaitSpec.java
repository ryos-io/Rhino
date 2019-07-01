package io.ryos.rhino.sdk.specs;

import java.time.Duration;

/**
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface WaitSpec extends Spec {

  Duration getWaitTime();
}
