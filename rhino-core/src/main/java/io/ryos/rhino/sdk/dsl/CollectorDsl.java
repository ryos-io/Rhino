package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.specs.Spec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CollectorDsl implements LoadDsl {

  private Duration pause;
  private String testName;

  private final List<Spec> executableFunctions =
      new ArrayList<>();

  @Override
  public CollectorDsl pause(Duration duration) {
    return null;
  }

  @Override
  public String getName() {
    return testName;
  }

  @Override
  public LoadDsl withName(final String testName) {
    executableFunctions.forEach(s -> s.setTestName(testName));
    this.testName = testName;
    return this;
  }

  @Override
  public CollectorDsl run(Spec executable) {
    executableFunctions.add(executable);
    return this;
  }

  public List<Spec> specs() {
    return executableFunctions;
  }
}
