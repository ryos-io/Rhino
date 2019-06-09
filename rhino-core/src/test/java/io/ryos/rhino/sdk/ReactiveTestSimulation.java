package io.ryos.rhino.sdk;

import io.ryos.rhino.sdk.annotations.Influx;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.TestSpec;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.specs.Spec;
import java.util.UUID;

@Simulation(name = "Reactive Test")
@Influx
@Runner(clazz = ReactiveHttpSimulationRunner.class)
public class ReactiveTestSimulation {

  private static final String HEALTH_ENDPOINT = "https://cc-api-storage-stage.adobe.io/server-status/health";

  @TestSpec(name="Spec")
  public Spec healthcheckCallSpec() {
    return Spec
        .http("Health Check")
        .target(HEALTH_ENDPOINT)
        .headers("X-Request-Id", "Rhino" + UUID.randomUUID().toString())
        .get()
        .andThen((session) -> {

          System.out.println(session.get("response"));

          return Spec
              .http("Step 2")
              .target(HEALTH_ENDPOINT)
              .get();
        });
  }
}
