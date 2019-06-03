package io.ryos.rhino.sdk;

import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.TestSpec;
import io.ryos.rhino.sdk.specs.Spec;

@Simulation(name="Reactive Test")
@Runner(clazz = ReactiveSimulationRunner.class)
public class ReactiveTestSimulation {

  @TestSpec
  public Spec discoveryCallSpec() {
    return Spec.http()
        .target("https://www.google.com")
        .queryParam("test", "value")
        .headers("X-Request-Id", "value")
        .get();
  }
}
