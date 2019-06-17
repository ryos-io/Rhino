package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.specs.Spec;
import java.time.Duration;

public interface LoadDsl {

  ConnectableDsl pause(Duration duration);

  ConnectableDsl run(Spec spec);
}
