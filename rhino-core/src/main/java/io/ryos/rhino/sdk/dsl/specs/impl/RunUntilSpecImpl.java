package io.ryos.rhino.sdk.dsl.specs.impl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.mat.RunUntilSpecMaterializer;
import io.ryos.rhino.sdk.dsl.specs.DSLItem;
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.RunUntilSpec;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class RunUntilSpecImpl extends AbstractMeasurableSpec implements RunUntilSpec {

  private final Predicate<UserSession> predicate;
  private final DSLSpec spec;

  public RunUntilSpecImpl(DSLSpec spec, Predicate<UserSession> predicate) {
    super("");

    this.predicate = predicate;
    this.spec = spec;
  }

  @Override
  public Predicate<UserSession> getPredicate() {
    return predicate;
  }

  @Override
  public DSLSpec getSpec() {
    return spec;
  }

  @Override
  public RunUntilSpecMaterializer createMaterializer(UserSession session) {
    return new RunUntilSpecMaterializer();
  }

  @Override
  public List<DSLItem> getChildren() {
    return Collections.emptyList();
  }
}
