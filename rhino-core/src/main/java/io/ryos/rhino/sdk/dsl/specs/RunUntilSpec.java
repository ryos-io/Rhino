package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.data.UserSession;
import java.util.function.Predicate;

public interface RunUntilSpec extends DSLSpec {

  Predicate<UserSession> getPredicate();

  DSLSpec getSpec();
}
