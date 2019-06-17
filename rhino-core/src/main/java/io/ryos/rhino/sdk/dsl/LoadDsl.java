package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.specs.Spec;
import java.time.Duration;
import java.util.List;

public interface LoadDsl {

  CollectorDsl pause(Duration duration);

  String getName();

  LoadDsl withName(String name);

  CollectorDsl run(Spec spec);

  public List<Spec> specs();
}
