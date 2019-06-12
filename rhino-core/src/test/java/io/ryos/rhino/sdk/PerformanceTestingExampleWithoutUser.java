package io.ryos.rhino.sdk;

import io.ryos.rhino.sdk.annotations.After;
import io.ryos.rhino.sdk.annotations.Before;
import io.ryos.rhino.sdk.annotations.Feeder;
import io.ryos.rhino.sdk.annotations.Influx;
import io.ryos.rhino.sdk.annotations.Scenario;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.feeders.UUIDProvider;
import io.ryos.rhino.sdk.reporting.Measurement;
import javax.ws.rs.core.Response.Status;

@Simulation(name = "Server-Status SimulationMetadata Without User")
public class PerformanceTestingExampleWithoutUser {

  @Feeder(factory = UUIDProvider.class)
  private String uuid;

  @Before
  public void prepare() {
    //System.out.println("Preparing the test with user:" + user.getUsername());
  }

  private void waitASec() {
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
    }
  }

  @Scenario(name = "scenario2")
  public void scenario2(Measurement measurement) throws InterruptedException {
    //System.out.println("scenario2 - on " + Thread.currentThread().getName());
    waitASec();
    measurement.measure("a2", Status.OK.toString());
    waitASec();
    measurement.measure("discovery call", Status.OK.toString());
  }

  @After
  public void cleanUp() {
    // System.out.println("Clean up the test with user:" + user.getUsername());
  }
}
