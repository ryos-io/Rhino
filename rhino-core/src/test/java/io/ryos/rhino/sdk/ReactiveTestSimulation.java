package io.ryos.rhino.sdk;

import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.TestSpec;
import io.ryos.rhino.sdk.specs.Spec;

@Simulation
@Runner(clazz = ReactiveSimulationRunner.class)
public class ReactiveTestSimulation {

  @TestSpec
  public Spec discoveryCallSpec() {
    return new Spec() {
      @Override
      public int hashCode() {
        return super.hashCode();
      }
    };
  }

}
