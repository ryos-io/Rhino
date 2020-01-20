package io.ryos.rhino.sdk.dsl.impl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.mat.RunUntilDslMaterializer;
import io.ryos.rhino.sdk.dsl.DslItem;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.RunUntilDsl;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang3.Validate;

public class RunUntilDslImpl extends AbstractMeasurableDsl implements RunUntilDsl {

  private static final String BLANK = "";
  private final Predicate<UserSession> predicate;
  private final MaterializableDslItem spec;

  public RunUntilDslImpl(MaterializableDslItem spec, Predicate<UserSession> predicate) {
    super(BLANK);

    this.predicate = Validate.notNull(predicate, "Predicate must not be null.");
    this.spec = Validate.notNull(spec, "Spec must not be null.");
  }

  @Override
  public Predicate<UserSession> getPredicate() {
    return predicate;
  }

  @Override
  public MaterializableDslItem getSpec() {
    return spec;
  }

  @Override
  public RunUntilDslMaterializer materializer(UserSession session) {
    return new RunUntilDslMaterializer();
  }

  @Override
  public List<DslItem> getChildren() {
    return Collections.emptyList();
  }
}
