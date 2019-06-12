package io.ryos.rhino.sdk;

import org.junit.Ignore;
import org.junit.Test;

public class PerformanceScenarioSpecImplTest {

  @Test
  public void testRun() {

    Simulation
        .create("classpath:///rhino.properties", "Server-Status SimulationMetadata Without User")
        .start();
  }
}
