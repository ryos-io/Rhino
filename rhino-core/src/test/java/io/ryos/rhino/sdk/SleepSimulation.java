package io.ryos.rhino.sdk;

import io.ryos.rhino.sdk.annotations.After;
import io.ryos.rhino.sdk.annotations.Before;
import io.ryos.rhino.sdk.annotations.Feeder;
import io.ryos.rhino.sdk.annotations.Influx;
import io.ryos.rhino.sdk.annotations.Scenario;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserFeeder;
import io.ryos.rhino.sdk.feeders.UUIDProvider;
import io.ryos.rhino.sdk.reporting.Measurement;
import io.ryos.rhino.sdk.users.data.User;
import javax.ws.rs.core.Response.Status;

@Simulation(name = "Server-Status Simulation Without User", durationInMins = 3)
@Influx
public class SleepSimulation {

  @Feeder(factory = UUIDProvider.class)
  private String uuid;

  @UserFeeder
  private User user;

  @Before
  public void prepare() {
    // System.out.println("Preparing the test with user:" + user.getUsername());
  }

  @Scenario(name = "Sleep Scenario")
  public void sleepTest(Measurement measurement) {
    await(1000L);
    measurement.measure("Sleep 1000", Status.OK.toString());
    await(500);
    measurement.measure("Sleep 500", Status.NOT_FOUND.toString());
    await(100);
    measurement.measure("Sleep 10", Status.MOVED_PERMANENTLY.toString());
  }

  @After
  public void cleanUp() {
    // System.out.println("Clean up the test with user:" + user.getUsername());
  }

  private void await(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      //
    }
  }
}
