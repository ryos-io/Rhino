package io.ryos.rhino.sdk;

import io.ryos.rhino.sdk.simulations.FilterSimulation;
import org.junit.Test;

public class FilterSimulationTest {

  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Test
  public void testForEachSimulation() {
    Simulation.getInstance(PROPERTIES_FILE, FilterSimulation.class).start();
  }
}
