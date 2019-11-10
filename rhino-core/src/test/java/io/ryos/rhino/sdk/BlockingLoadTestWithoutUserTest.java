package io.ryos.rhino.sdk;

import io.ryos.rhino.sdk.simulations.BlockingLoadTestWithoutUserSimulation;
import org.junit.Test;

public class BlockingLoadTestWithoutUserTest {

  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Test
  public void testBlockingLoadTestWithoutUser() {
    Simulation.getInstance(PROPERTIES_FILE, BlockingLoadTestWithoutUserSimulation.class).start();
  }
}
