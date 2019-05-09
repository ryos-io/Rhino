package io.ryos.rhino.sdk;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PerformanceScenarioSpecImplTest {

  @Test
  public void testRun() throws InterruptedException {

    SimulationSpec simulation = new SimulationSpecImpl("classpath:///rhino.properties", "Server-Status Simulation Without User");
    simulation.start();
    Thread.sleep(10000L);
    simulation.stop();
    Thread.sleep(5000L);
  }
}
