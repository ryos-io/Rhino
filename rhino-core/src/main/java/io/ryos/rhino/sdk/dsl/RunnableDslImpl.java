package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.Spec;
import io.ryos.rhino.sdk.dsl.specs.impl.ConditionalSpecWrapper;
import io.ryos.rhino.sdk.dsl.specs.impl.ForEachBuilder;
import io.ryos.rhino.sdk.dsl.specs.impl.ForEachSpecImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.MapperBuilder;
import io.ryos.rhino.sdk.dsl.specs.impl.MapperSpecImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.WaitSpecImpl;
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
public class RunnableDslImpl implements LoadDsl, RunnableDsl {

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
  public RunnableDslImpl wait(Duration duration) {
    executableFunctions.add(new WaitSpecImpl(duration));
    return this;
  }

  @Override
  public RunnableDsl run(Spec spec) {
    executableFunctions.add(spec);
    return this;
  }

  @Override
  public <R, T> RunnableDsl map(MapperBuilder<R, T> mapper) {
    executableFunctions.add(new MapperSpecImpl<>(mapper));
    return this;
  }

  @Override
  public <E, R extends Iterable<E>> RunnableDsl forEach(ForEachBuilder<E, R> forEachBuilder) {
    executableFunctions.add(new ForEachSpecImpl<>(forEachBuilder));
    return this;
  }

  @Override
  public RunnableDsl runIf(Predicate<UserSession> predicate, Spec spec) {
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
