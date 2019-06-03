package io.ryos.rhino.sdk;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PerformanceScenarioSpecImplTest {

  @Test
  public void testRun() {

    SimulationSpec simulation = new SimulationSpecImpl("classpath:///rhino.properties",
        "Server-Status Simulation Without User");
  }
}
