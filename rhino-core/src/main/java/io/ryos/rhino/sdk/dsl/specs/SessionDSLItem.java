package io.ryos.rhino.sdk.dsl.specs;

import java.util.function.Supplier;

public interface SessionDSLItem extends DSLSpec {

  Scope getSessionScope();

  String getSessionKey();

  void setSessionKey(String key);

  Supplier<Object> getObjectFunction();

  void setSessionScope(Scope scope);

  enum Scope {
    USER,
    SIMULATION,
    EPHEMERAL
  }

}
