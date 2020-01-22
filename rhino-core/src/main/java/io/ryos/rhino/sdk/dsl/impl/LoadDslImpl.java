package io.ryos.rhino.sdk.dsl.impl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.AssertionDsl;
import io.ryos.rhino.sdk.dsl.DslItem;
import io.ryos.rhino.sdk.dsl.IterableDsl;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.MappableDsl;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.SessionDsl;
import io.ryos.rhino.sdk.dsl.data.builder.ForEachBuilder;
import io.ryos.rhino.sdk.dsl.data.builder.MapperBuilder;
import io.ryos.rhino.sdk.dsl.mat.DslMaterializer;
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
public class LoadDslImpl extends AbstractDSLItem implements SessionDsl, LoadDsl,
    IterableDsl, AssertionDsl, MappableDsl, MaterializableDslItem {

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
  public LoadDsl run(MaterializableDslItem spec) {
    Validate.notNull(spec, "Spec must not be null.");
    children.add(spec);
    return this;
  }

  @Override
  public LoadDsl ensure(Predicate<UserSession> predicate) {
    Validate.notNull(predicate, "Predicate must not be null.");
    children.add(new EnsureDslImpl(predicate));
    return this;
  }

  @Override
  public LoadDsl ensure(Predicate<UserSession> predicate, String reason) {
    Validate.notNull(predicate, "Predicate must not be null.");
    Validate.notNull(reason, "Reason must not be null.");
    children.add(new EnsureDslImpl(predicate, reason));
    return this;
  }

  @Override
  public LoadDsl session(String sessionKey, Supplier<Object> objectSupplier) {
    Validate.notNull(objectSupplier, "Object supplier must not be null.");
    Validate.notEmpty(sessionKey, "Session key must not be null.");
    children.add(new SessionDslImpl(sessionKey, objectSupplier));
    return this;
  }

  @Override
  public <R, T> LoadDsl map(MapperBuilder<R, T> mapperBuilder) {
    Validate.notNull(mapperBuilder, "Mapper builder must not be null.");
    children.add(new MapperDslImpl<>(mapperBuilder));
    return this;
  }

  @Override
  public <E, R extends Iterable<E>> LoadDsl forEach(String name,
      ForEachBuilder<E, R> forEachBuilder) {
    Validate.notNull(forEachBuilder, "For each builder must not be null.");
    Validate.notEmpty(name, "Name must not be null.");

    children.add(new ForEachDslImpl(name, Collections.emptyList(),
        forEachBuilder.getSessionKey() != null ? forEachBuilder.getSessionKey() : name,
        forEachBuilder.getSessionScope(),
        forEachBuilder.getIterableSupplier(),
        forEachBuilder.getForEachFunction()));
    return this;
  }

  @Override
  public LoadDsl repeat(MaterializableDslItem spec) {
    Validate.notNull(spec, "Spec must not be null.");
    children.add(new RunUntilDslImpl(spec, (s) -> true));
    return this;
  }

  @Override
  public LoadDsl until(Predicate<UserSession> predicate, MaterializableDslItem spec) {
    Validate.notNull(spec, "Spec must not be null.");
    Validate.notNull(predicate, "Predicate must not be null.");
    children.add(new RunUntilDslImpl(spec, predicate));
    return this;
  }

  @Override
  public LoadDsl asLongAs(Predicate<UserSession> predicate, MaterializableDslItem spec) {
    Validate.notNull(spec, "Spec must not be null.");
    Validate.notNull(predicate, "Predicate must not be null.");
    children.add(new RunUntilDslImpl(spec, (s) -> !predicate.test(s)));
    return this;
  }

  @Override
  public LoadDsl runIf(Predicate<UserSession> predicate, MaterializableDslItem spec) {
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

  @Override
  public <T extends MaterializableDslItem> DslMaterializer<T> materializer(
      UserSession userSession) {
    return null;
  }
}
