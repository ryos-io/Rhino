package io.ryos.rhino.sdk;

import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.TestSpec;
import io.ryos.rhino.sdk.runners.ReactiveSimulationRunner;
import io.ryos.rhino.sdk.specs.Spec;
import java.util.UUID;

@Simulation(name = "Reactive Test")
@Runner(clazz = ReactiveSimulationRunner.class)
public class ReactiveTestSimulation {

  private static final String HEALTH_ENDPOINT = "https://cc-api-storage-stage.adobe"
      + ".io/server-status/health";

  @TestSpec(name="Spec")
  public Spec healthcheckCallSpec() {
    return Spec.http("Health Check")
        .target(HEALTH_ENDPOINT)
        .headers("X-Request-Id", "Rhino" + UUID.randomUUID().toString())
        .get();
  }
}
