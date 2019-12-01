package io.ryos.rhino.sdk.dsl.specs;

import java.time.Duration;

/**
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface WaitSpec extends DSLSpec {

  Duration getWaitTime();
}
