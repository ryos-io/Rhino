package io.ryos.rhino.sdk;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ReactiveBasicHttpGetTest {

  private static final String SIM_NAME = "Reactive Test";
  private static final String PROPERTIES_FILE = "classpath:///reactive_basic_http.properties";

  @Test
  public void testReactiveBasicHttp() {
    Simulation.create(PROPERTIES_FILE, SIM_NAME).start();
  }
}
