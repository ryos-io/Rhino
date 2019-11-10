package io.ryos.rhino.sdk.dsl.specs.impl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.mat.RunUntilSpecMaterializer;
import io.ryos.rhino.sdk.dsl.specs.RunUntilSpec;
import io.ryos.rhino.sdk.dsl.specs.Spec;
import java.util.function.Predicate;

public class RunUntilSpecImpl extends AbstractSpec implements RunUntilSpec {

  private final Predicate<UserSession> predicate;
  private final Spec spec;

  public RunUntilSpecImpl(Spec spec, Predicate<UserSession> predicate) {
    super("");

    this.predicate = predicate;
    this.spec = spec;
  }

  @Override
  public Predicate<UserSession> getPredicate() {
    return predicate;
  }

  @Override
  public Spec getSpec() {
    return spec;
  }

  @Override
  public RunUntilSpecMaterializer createMaterializer(UserSession session) {
    return new RunUntilSpecMaterializer();
  }
}
