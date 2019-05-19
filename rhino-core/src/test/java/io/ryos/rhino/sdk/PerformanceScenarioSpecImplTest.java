package io.ryos.rhino.sdk;

import org.junit.Test;

public class PerformanceScenarioSpecImplTest {

  @Test
  public void testRun() throws InterruptedException {

    SimulationSpec simulation = new SimulationSpecImpl("classpath:///rhino.properties",
        "Server-Status Simulation Without User");
  }
}
