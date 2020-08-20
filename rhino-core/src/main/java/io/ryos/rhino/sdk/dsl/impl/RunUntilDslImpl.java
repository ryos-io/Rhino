package io.ryos.rhino.sdk.dsl.impl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.RunUntilDsl;
import io.ryos.rhino.sdk.dsl.mat.RunUntilDslMaterializer;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang3.Validate;

public class RunUntilDslImpl extends AbstractMeasurableDsl implements RunUntilDsl {

  private static final String BLANK = "";
  private final MaterializableDslItem spec;

  private Predicate<UserSession> predicate;
  private int maxRepeat;

  public RunUntilDslImpl(MaterializableDslItem spec, Predicate<UserSession> predicate) {
    super(BLANK);

    this.predicate = Validate.notNull(predicate, "Predicate must not be null.");
    this.spec = Validate.notNull(spec, "Spec must not be null.");
  }

  public RunUntilDslImpl(MaterializableDslItem spec, int maxRepeat) {
    super(BLANK);

    Validate.isTrue(maxRepeat > 0, "repeat count must be > 0", maxRepeat);
    this.spec = Validate.notNull(spec, "Spec must not be null.");
    this.maxRepeat = maxRepeat;
  }

  @Override
  public int getMaxRepeat() {
    return maxRepeat;
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
  public RunUntilDslMaterializer materializer() {
    return new RunUntilDslMaterializer(this);
  }

  @Override
  public List<MaterializableDslItem> getChildren() {
    return Collections.emptyList();
  }
}
