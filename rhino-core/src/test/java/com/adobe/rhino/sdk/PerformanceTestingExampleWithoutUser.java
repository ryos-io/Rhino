package com.adobe.rhino.sdk;

import com.adobe.rhino.sdk.annotations.After;
import com.adobe.rhino.sdk.annotations.Feeder;
import com.adobe.rhino.sdk.annotations.Logging;
import com.adobe.rhino.sdk.annotations.Before;
import com.adobe.rhino.sdk.annotations.Scenario;
import com.adobe.rhino.sdk.annotations.Simulation;
import com.adobe.rhino.sdk.feeders.UUIDFeeder;
import com.adobe.rhino.sdk.reporting.GatlingLogFormatter;

@Simulation(name = "Server-Status Simulation Without User")
@Logging(file = "/Users/bagdemir/sims/simulation.log", formatter = GatlingLogFormatter.class)
public class PerformanceTestingExampleWithoutUser {

  @Feeder(factory = UUIDFeeder.class)
  private String uuid;

  @Before
  public void prepare() {
    //System.out.println("Preparing the test with user:" + user.getUsername());
  }

  @Scenario(name = "Hello")
  public void performDiscovery(Recorder recorder) {
    System.out.println("Hello");
  }

  @After
  public void cleanUp() {
    // System.out.println("Clean up the test with user:" + user.getUsername());
  }
}
