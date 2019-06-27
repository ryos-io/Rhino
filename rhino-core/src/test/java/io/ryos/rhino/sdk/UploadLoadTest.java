package io.ryos.rhino.sdk;

import org.junit.Test;

public class UploadLoadTest {


  private static final String SIM_NAME = "Reactive Upload Test";
  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Test
  public void testReactiveBasicHttp() {
    Simulation.create(PROPERTIES_FILE, SIM_NAME).start();
  }
}
