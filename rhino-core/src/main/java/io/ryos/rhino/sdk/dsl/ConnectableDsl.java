package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.specs.ConditionalSpecWrapper;
import io.ryos.rhino.sdk.specs.MapperBuilder;
import io.ryos.rhino.sdk.specs.MapperSpecImpl;
import io.ryos.rhino.sdk.specs.Spec;
import io.ryos.rhino.sdk.specs.WaitSpecImpl;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Connectable DSL, is the DSL instance to bind chaining specs.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class ConnectableDsl implements LoadDsl, ConfigurableDsl {

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
  public ConfigurableDsl run(Spec spec) {
    executableFunctions.add(spec);
    return this;
  }

  @Override
  public <R, T> ConfigurableDsl map(MapperBuilder<R, T> mapper) {
    executableFunctions.add(new MapperSpecImpl<>(mapper));
    return this;
  }

  @Override
  public ConfigurableDsl runIf(Predicate<UserSession> predicate, Spec spec) {
    executableFunctions.add(new ConditionalSpecWrapper(spec, predicate));
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
