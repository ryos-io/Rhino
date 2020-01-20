package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.DslItem;
import io.ryos.rhino.sdk.dsl.specs.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.specs.builder.ForEachBuilder;
import io.ryos.rhino.sdk.dsl.specs.builder.MapperBuilder;
import io.ryos.rhino.sdk.dsl.specs.impl.AbstractDSLItem;
import io.ryos.rhino.sdk.dsl.specs.impl.ConditionalDslWrapper;
import io.ryos.rhino.sdk.dsl.specs.impl.EnsureDslImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.ForEachDslImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.MapperDslImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.RunUntilDslImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.SessionDslImpl;
import io.ryos.rhino.sdk.dsl.specs.impl.WaitDslImpl;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.lang3.Validate;

/**
 * Connectable DSL, is the DSL instance to bind chaining specs.
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class LoadDslImpl extends AbstractDSLItem implements LoadDsl, SessionDsl, RunnableDsl, IterableDsl, AssertionDsl {

  /**
   * Executable functions.
   */
  private final List<DslItem> children = new ArrayList<>();

  public LoadDslImpl(String name) {
    super(name);
  }

  @Override
  public LoadDslImpl wait(Duration duration) {
    Validate.notNull(duration, "Duration must not be null.");
    children.add(new WaitDslImpl(duration));
    return this;
  }

  @Override
  public RunnableDsl run(MaterializableDslItem spec) {
    Validate.notNull(spec, "Spec must not be null.");
    children.add(spec);
    return this;
  }

  @Override
  public RunnableDsl ensure(Predicate<UserSession> predicate) {
    Validate.notNull(predicate, "Predicate must not be null.");
    children.add(new EnsureDslImpl(predicate));
    return this;
  }

  @Override
  public RunnableDsl ensure(Predicate<UserSession> predicate, String reason) {
    Validate.notNull(predicate, "Predicate must not be null.");
    Validate.notNull(reason, "Reason must not be null.");
    children.add(new EnsureDslImpl(predicate, reason));
    return this;
  }

  @Override
  public RunnableDsl session(String sessionKey, Supplier<Object> objectSupplier) {
    Validate.notNull(objectSupplier, "Object supplier must not be null.");
    Validate.notEmpty(sessionKey, "Session key must not be null.");
    children.add(new SessionDslImpl(sessionKey, objectSupplier));
    return this;
  }

  @Override
  public <R, T> RunnableDsl map(MapperBuilder<R, T> mapperBuilder) {
    Validate.notNull(mapperBuilder, "Mapper builder must not be null.");
    children.add(new MapperDslImpl<>(mapperBuilder));
    return this;
  }

  @Override
  public <E, R extends Iterable<E>> RunnableDsl forEach(String name,
      ForEachBuilder<E, R> forEachBuilder) {
    Validate.notNull(forEachBuilder, "For each builder must not be null.");
    Validate.notEmpty(name, "Name must not be null.");

    children.add(new ForEachDslImpl<>(name, Collections.emptyList(),
        forEachBuilder.getKey() != null ? forEachBuilder.getKey() : name,
        forEachBuilder.getScope(),
        forEachBuilder.getIterableSupplier(),
        forEachBuilder.getForEachFunction()));
    return this;
  }

  @Override
  public RunnableDsl repeat(MaterializableDslItem spec) {
    Validate.notNull(spec, "Spec must not be null.");
    children.add(new RunUntilDslImpl(spec, (s) -> true));
    return this;
  }

  @Override
  public RunnableDsl until(Predicate<UserSession> predicate, MaterializableDslItem spec) {
    Validate.notNull(spec, "Spec must not be null.");
    Validate.notNull(predicate, "Predicate must not be null.");
    children.add(new RunUntilDslImpl(spec, predicate));
    return this;
  }

  @Override
  public RunnableDsl asLongAs(Predicate<UserSession> predicate, MaterializableDslItem spec) {
    Validate.notNull(spec, "Spec must not be null.");
    Validate.notNull(predicate, "Predicate must not be null.");
    children.add(new RunUntilDslImpl(spec, (s) -> !predicate.test(s)));
    return this;
  }

  @Override
  public RunnableDsl runIf(Predicate<UserSession> predicate, MaterializableDslItem spec) {
    Validate.notNull(spec, "Spec must not be null.");
    Validate.notNull(predicate, "Predicate must not be null.");
    children.add(new ConditionalDslWrapper(spec, predicate));
    return this;
  }

  public LoadDsl withName(final String dslName) {
    Validate.notEmpty(dslName, "DSL name must not be null.");
    super.setName(dslName);
    return this;
  }

  public List<DslItem> getChildren() {
    return children;
  }
}
