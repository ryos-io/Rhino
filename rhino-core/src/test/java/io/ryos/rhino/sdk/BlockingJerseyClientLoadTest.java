package io.ryos.rhino.sdk;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class BlockingJerseyClientLoadTest {

  private static final String SIM_NAME = "Server-Status Simulation";
  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Test
  public void testReactiveBasicHttp() {
    Simulation.create(PROPERTIES_FILE, SIM_NAME).start();
  }
}
