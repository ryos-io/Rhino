package io.ryos.rhino.sdk;

import org.junit.Test;

public class BlockingLoadTestWithoutUserTest {

  private static final String SIM_NAME = "Server-Status Simulation Without User";
  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Test
  public void testBlockingLoadTestWithoutUser() {
    Simulation.create(PROPERTIES_FILE, SIM_NAME).start();
  }
}
