package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.specs.Spec;
import java.time.Duration;

public interface LoadDsl {

  CollectorDsl pause(Duration duration);

  CollectorDsl run(Spec spec);
}
