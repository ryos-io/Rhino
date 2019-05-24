package io.ryos.rhino.sdk;

import io.ryos.rhino.sdk.annotations.After;
import io.ryos.rhino.sdk.annotations.Feeder;
import io.ryos.rhino.sdk.annotations.Before;
import io.ryos.rhino.sdk.annotations.Scenario;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.feeders.UUIDProvider;
import io.ryos.rhino.sdk.reporting.Recorder;

@Simulation(name = "Server-Status Simulation Without User")
public class PerformanceTestingExampleWithoutUser {

  @Feeder(factory = UUIDProvider.class)
  private String uuid;

  @Before
  public void prepare() {
    //System.out.println("Preparing the test with user:" + user.getUsername());
  }

  @Scenario(name = "Hellolololoolololloloolollolo")
  public void performDiscovery(Recorder recorder) throws InterruptedException {
    // System.out.println("Hello");
    //Thread.sleep(1000L);
    recorder.record("test1 as as as as as as as as as", 200);
    //Thread.sleep(100L);
    recorder.record("test1", 404);
    //Thread.sleep(100L);
    recorder.record("test2", 200);

  }

  @After
  public void cleanUp() {
    // System.out.println("Clean up the test with user:" + user.getUsername());
  }
}
