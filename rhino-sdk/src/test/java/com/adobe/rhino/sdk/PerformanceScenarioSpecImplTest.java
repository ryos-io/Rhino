package com.adobe.rhino.sdk;

import com.adobe.rhino.sdk.utils.Environment;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PerformanceScenarioSpecImplTest {

  @Test
  public void testRun() throws InterruptedException {

    SimulationSpec simulation = new SimulationSpecImpl("classpath:///rhino.properties", "Server-Status Simulation");
    simulation.start();
    Thread.sleep(10000L);
    simulation.stop();
    Thread.sleep(5000L);
  }
}
