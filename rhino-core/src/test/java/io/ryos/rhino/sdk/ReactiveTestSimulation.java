package io.ryos.rhino.sdk;

import static io.ryos.rhino.sdk.specs.Spec.http;

import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Influx;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;

@Simulation(name = "Reactive Test")
@Influx
@Runner(clazz = ReactiveHttpSimulationRunner.class)
public class ReactiveTestSimulation {

  private static final String HEALTH_ENDPOINT = "https://cc-api-storage-stage.adobe.io/server-status/health";

  @Dsl(name = "Spec")
  public LoadDsl testDsl() {
    return Start
        .spec()
        .run(http("first call").endpoint((r) -> "http://bagdemir.com").get())
        .run(http("second call").endpoint((r) -> "http://google.com").get());
  }
}
