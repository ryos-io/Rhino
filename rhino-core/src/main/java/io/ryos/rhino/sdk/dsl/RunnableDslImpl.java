package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.DSLItem;
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.builder.ForEachBuilder;
import io.ryos.rhino.sdk.dsl.specs.builder.MapperBuilder;
import io.ryos.rhino.sdk.dsl.specs.impl.AbstractDSLItem;
import io.ryos.rhino.sdk.dsl.specs.impl.ConditionalSpecWrapper;
import io.ryos.rhino.sdk.dsl.specs.impl.EnsureSpecImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.ForEachSpecImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.MapperSpecImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.RunUntilSpecImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.SessionSpecImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.WaitSpecImpl;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Connectable DSL, is the DSL instance to bind chaining specs.
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class RunnableDslImpl extends AbstractDSLItem implements LoadDsl, RunnableDsl, IterableDsl {

  /**
   * Executable functions.
   */
  private final List<DSLItem> children = new ArrayList<>();

  public RunnableDslImpl(String name) {
    super(name);
  }

  @Override
  public RunnableDslImpl wait(Duration duration) {
    children.add(new WaitSpecImpl(duration));
    return this;
  }

  @Override
  public RunnableDsl run(DSLSpec spec) {
    children.add(spec);
    return this;
  }

  @Override
  public RunnableDsl ensure(Predicate<UserSession> predicate) {
    children.add(new EnsureSpecImpl(predicate));
    return this;
  }

  @Override
  public RunnableDsl ensure(Predicate<UserSession> predicate, String reason) {
    children.add(new EnsureSpecImpl(predicate, reason));
    return this;
  }

  @Override
  public RunnableDsl session(String key, Supplier<Object> objectSupplier) {
    children.add(new SessionSpecImpl(key, objectSupplier));
    return this;
  }

  @Override
  public <R, T> RunnableDsl map(MapperBuilder<R, T> mapper) {
    children.add(new MapperSpecImpl<>(mapper));
    return this;
  }

  @Override
  public <E, R extends Iterable<E>> RunnableDsl forEach(String name,
      ForEachBuilder<E, R> forEachBuilder) {
    children.add(new ForEachSpecImpl<>(name, Collections.emptyList(),
        forEachBuilder.getKey() != null ? forEachBuilder.getKey() : name,
        forEachBuilder.getScope(),
        forEachBuilder.getIterableSupplier(),
        forEachBuilder.getForEachFunction()));
    return this;
  }

  @Override
  public RunnableDsl repeat(DSLSpec spec) {
    children.add(new RunUntilSpecImpl(spec, (s) -> true));
    return this;
  }

  @Override
  public RunnableDsl runUntil(Predicate<UserSession> predicate, DSLSpec spec) {
    children.add(new RunUntilSpecImpl(spec, predicate));
    return this;
  }

  @Override
  public RunnableDsl runAsLongAs(Predicate<UserSession> predicate, DSLSpec spec) {
    children.add(new RunUntilSpecImpl(spec, (s) -> !predicate.test(s)));
    return this;
  }

  @Override
  public RunnableDsl runIf(Predicate<UserSession> predicate, DSLSpec spec) {
    children.add(new ConditionalSpecWrapper(spec, predicate));
    return this;
  }

  public LoadDsl withName(final String dslName) {
    super.setName(dslName);
    return this;
  }

  public List<DSLItem> getChildren() {
    return children;
  }
}
