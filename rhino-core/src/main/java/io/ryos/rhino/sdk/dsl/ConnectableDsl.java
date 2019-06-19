package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.specs.Spec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ConnectableDsl implements LoadDsl {

  private Duration pause;

  private String testName;

  private final List<Spec> executableFunctions = new ArrayList<>();

  @Override
  public ConnectableDsl pause(Duration duration) {
    this.pause = duration;
    return this;
  }

  @Override
  public ConnectableDsl run(Spec executable) {
    executableFunctions.add(executable);
    return this;
  }

  public LoadDsl withName(final String testName) {
    executableFunctions.forEach(s -> s.setTestName(testName));
    this.testName = testName;
    return this;
  }

  public String getName() {
    return testName;
  }

  public List<Spec> getSpecs() {
    return executableFunctions;
  }

  public Duration getPause() {
    return pause;
  }
}
