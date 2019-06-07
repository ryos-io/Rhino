package io.ryos.rhino.sdk;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PerformanceScenarioSpecImplTest {

  @Test
  public void testRun() {

    SimulationSpec
        .create("classpath:///rhino.properties", "Reactive Test")
        .start();
  }
}
