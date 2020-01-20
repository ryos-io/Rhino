package io.ryos.rhino.sdk.dsl;

import java.time.Duration;

/**
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface WaitDsl extends MaterializableDslItem {

  Duration getWaitTime();
}
