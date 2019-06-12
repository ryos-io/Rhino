package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.specs.HttpSpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CollectorDsl implements LoadDsl {

  private Duration pause;
  private String testName;

  private final List<HttpSpec> executableFunctions =
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
  public CollectorDsl run(HttpSpec executable) {
    executableFunctions.add(executable);
    return this;
  }

  public List<HttpSpec> specs() {
    return executableFunctions;
  }
}
