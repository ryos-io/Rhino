package io.ryos.rhino.sdk.dsl.impl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.data.builder.ForEachBuilder;
import io.ryos.rhino.sdk.dsl.data.builder.MapperBuilder;
import io.ryos.rhino.sdk.dsl.mat.LoadDslMaterializer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.lang3.Validate;

/**
 * Load DSL implementation, that is the container DSL instance to bind other DSL items.
 *
 * @author Erhan Bagdemir
 */
public class LoadDslImpl extends AbstractDSLItem implements LoadDsl {

  /**
   * Executable functions.
   */
  private final List<MaterializableDslItem> children = new ArrayList<>();

  public LoadDslImpl(String name) {
    super(name);
  }

  @Override
  public LoadDsl wait(Duration duration) {
    Validate.notNull(duration, "Duration must not be null.");
    children.add(new WaitDslImpl(duration));
    return this;
  }

  @Override
  public LoadDsl run(MaterializableDslItem dslItem) {
    Validate.notNull(dslItem, "dslItem must not be null.");

    dslItem.setParent(this);
    children.add(dslItem);
    return this;
  }

  @Override
  public LoadDsl ensure(Predicate<UserSession> predicate) {
    Validate.notNull(predicate, "Predicate must not be null.");

    var ensureDsl = new EnsureDslImpl(predicate);
    children.add(ensureDsl);
    return this;
  }

  @Override
  public LoadDsl ensure(Predicate<UserSession> predicate, String reason) {
    Validate.notNull(predicate, "Predicate must not be null.");
    Validate.notNull(reason, "Reason must not be null.");

    var ensureDsl = new EnsureDslImpl(predicate, reason);
    children.add(ensureDsl);
    return this;
  }

  @Override
  public LoadDsl session(String sessionKey, Supplier<Object> objectSupplier) {
    Validate.notNull(objectSupplier, "Object supplier must not be null.");
    Validate.notEmpty(sessionKey, "Session key must not be null.");

    var sessionDsl = new SessionDslImpl(sessionKey, objectSupplier);
    sessionDsl.setParent(this);
    children.add(sessionDsl);
    return this;
  }

  @Override
  public LoadDsl session(final String sessionKey, final Object object) {
    Validate.notNull(object, "Object supplier must not be null.");
    Validate.notEmpty(sessionKey, "Session key must not be null.");

    var sessionDsl = new SessionDslImpl(sessionKey, () -> object);
    sessionDsl.setParent(this);
    children.add(sessionDsl);
    return this;
  }

  @Override
  public <R, T> LoadDsl map(MapperBuilder<R, T> mapperBuilder) {
    Validate.notNull(mapperBuilder, "Mapper builder must not be null.");
    MapperDslImpl<R, T> rtMapperDsl = new MapperDslImpl<>(mapperBuilder);
    rtMapperDsl.setParent(this);
    children.add(rtMapperDsl);
    return this;
  }

  @Override
  public <E, R extends Iterable<E>> LoadDsl forEach(String name,
      ForEachBuilder<E, R> forEachBuilder) {
    Validate.notNull(forEachBuilder, "For each builder must not be null.");
    Validate.notEmpty(name, "Name must not be null.");

    var forEachDsl = new ForEachDslImpl(name, Collections.emptyList(),
        forEachBuilder.getSessionKey() != null ? forEachBuilder.getSessionKey() : name,
        forEachBuilder.getSessionScope(),
        forEachBuilder.getIterableSupplier(),
        forEachBuilder.getForEachFunction(),
        forEachBuilder.getMapper());

    forEachDsl.setParent(this);
    children.add(forEachDsl);
    return this;
  }

  @Override
  public <E, R extends Iterable<E>> LoadDsl forEach(final ForEachBuilder<E, R> forEachBuilder) {
    return forEach("forEach-" + UUID.randomUUID(), forEachBuilder);
  }

  @Override
  public LoadDsl repeat(MaterializableDslItem dslItem) {
    Validate.notNull(dslItem, "dslItem must not be null.");

    RunUntilDslImpl runUntilDsl = new RunUntilDslImpl(dslItem, s -> true);
    runUntilDsl.setParent(this);
    dslItem.setParent(runUntilDsl);
    children.add(runUntilDsl);
    return this;
  }

  @Override
  public LoadDsl until(Predicate<UserSession> predicate, MaterializableDslItem dslItem) {
    Validate.notNull(dslItem, "dslItem must not be null.");
    Validate.notNull(predicate, "Predicate must not be null.");

    var runUntilDsl = new RunUntilDslImpl(dslItem, predicate);
    runUntilDsl.setParent(this);
    dslItem.setParent(runUntilDsl);
    children.add(runUntilDsl);
    return this;
  }

  @Override
  public LoadDsl asLongAs(Predicate<UserSession> predicate, MaterializableDslItem dslItem) {
    Validate.notNull(dslItem, "dslItem must not be null.");
    Validate.notNull(predicate, "Predicate must not be null.");

    var runUntilDsl = new RunUntilDslImpl(dslItem, (s) -> !predicate.test(s));
    runUntilDsl.setParent(this);
    dslItem.setParent(runUntilDsl);
    children.add(runUntilDsl);
    return this;
  }

  @Override
  public LoadDsl runIf(Predicate<UserSession> predicate, MaterializableDslItem dslItem) {
    Validate.notNull(dslItem, "dslItem must not be null.");
    Validate.notNull(predicate, "Predicate must not be null.");

    var conditionalDslWrapper = new ConditionalDslWrapper(dslItem, predicate);
    conditionalDslWrapper.setParent(this);
    dslItem.setParent(conditionalDslWrapper);
    children.add(conditionalDslWrapper);
    return this;
  }

  @Override
  public LoadDsl measure(final String tag, final MaterializableDslItem dslItem) {
    Validate.notNull(tag, "Tag must not be null.");
    Validate.notNull(dslItem, "dslItem must not be null.");

    var gaugeDsl = new GaugeDslImpl(tag, dslItem);
    gaugeDsl.setParent(this);
    dslItem.setParent(gaugeDsl);
    if (dslItem instanceof LoadDsl) {
      dslItem.setName(tag);
    }
    children.add(gaugeDsl);
    return this;
  }

  @Override
  public LoadDsl filter(Predicate<UserSession> predicate) {
    Validate.notNull(predicate, "Predicate must not be null.");

    var filterDsl = new FilterDslImpl(predicate);
    filterDsl.setParent(this);
    children.add(filterDsl);
    return this;
  }

  public LoadDsl withName(final String dslName) {
    Validate.notEmpty(dslName, "dslName must not be null.");
    super.setName(dslName);
    return this;
  }

  public List<MaterializableDslItem> getChildren() {
    return children;
  }

  @Override
  public LoadDslMaterializer materializer() {
    return new LoadDslMaterializer(this);
  }
}
