package io.ryos.rhino.sdk.monitoring;

import java.io.InputStream;

/**
 * TODO
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface GrafanaDashboard {

  String getDashboard(final String simulationName,
      final String dashboardTemplate,
      final String[] scenarios);

}
