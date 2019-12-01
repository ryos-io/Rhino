package io.ryos.rhino.sdk.simulations;

import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.RampUp;
import io.ryos.rhino.sdk.annotations.Scenario;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.providers.UUIDProvider;
import io.ryos.rhino.sdk.reporting.Measurement;
import javax.ws.rs.core.Response.Status;

@Simulation(name = "Server-Status Simulation Without User")
@RampUp(startRps = 10, targetRps = 30, durationInMins = 1)
public class BlockingLoadTestWithoutUserSimulation {

  @Provider(clazz = UUIDProvider.class)
  private UUIDProvider uuid;

  @Scenario(name = "scenario2")
  public void scenario2(Measurement measurement) {
    System.out.println("scenario2 - on " + Thread.currentThread().getName());
    measurement.measure("measurement1", Status.OK.toString());
  }
}
