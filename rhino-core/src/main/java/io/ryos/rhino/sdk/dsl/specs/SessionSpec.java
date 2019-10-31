package io.ryos.rhino.sdk.dsl.specs;

import java.util.function.Supplier;

public interface SessionSpec extends Spec {

  String getKey();

  Supplier<Object> getObjectFunction();
}
