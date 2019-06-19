package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.specs.Spec;
import io.ryos.rhino.sdk.specs.WaitSpecImpl;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Connectable DSL, is the DSL instance to bind chaining specs.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class ConnectableDsl implements LoadDsl {

  /**
   * Wait duration.
   * <p>
   */
  private Duration pause;

  /**
   * Test name, that is the name of the load DSL or scenario.
   * <p>
   */
  private String testName;

  /**
   * Executable functions.
   * <p>
   */
  private final List<Spec> executableFunctions = new ArrayList<>();

  @Override
  public ConnectableDsl wait(Duration duration) {
    executableFunctions.add(new WaitSpecImpl(duration));
    return this;
  }

  @Override
  public ConnectableDsl run(Spec spec) {
    executableFunctions.add(spec);
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
}
