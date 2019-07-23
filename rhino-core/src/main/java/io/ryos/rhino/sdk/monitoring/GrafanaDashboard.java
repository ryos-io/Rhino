package io.ryos.rhino.sdk.monitoring;

/**
 * Grafana dashboard representation. The dashboards will be created by using JSON templates while
 * starting the application, if Grafana integration is enabled.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface GrafanaDashboard {

  /**
   * Returns the string representation of the dashboard.
   * <p>
   *
   * @param simulationName The name of the simulation.
   * @param dashboardTemplate Dashboard template.
   * @param scenarios A list of scenarios used in dashboards.
   * @return String representation of the dashboard.
   */
  String getDashboard(String simulationName,
      String dashboardTemplate,
      String[] scenarios);
}
