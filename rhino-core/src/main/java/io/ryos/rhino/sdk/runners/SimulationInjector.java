package io.ryos.rhino.sdk.runners;

import io.ryos.rhino.sdk.feeders.Provider;

/**
 * Injector is a utility class is used to inject objects expected in injection points, that is
 * marked with {@link Provider} annotation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface SimulationInjector {

  /**
   * Applies the injections on injectable provided.
   * <p>
   *
   * @param injectable Injectable object.
   */
  void injectOn(final Object injectable);
}
