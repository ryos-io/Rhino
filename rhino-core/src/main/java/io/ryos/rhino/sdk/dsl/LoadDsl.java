package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.specs.Spec;
import java.time.Duration;
import java.util.function.Predicate;

public interface LoadDsl {

  ConnectableDsl wait(Duration duration);

  ConnectableDsl run(Spec spec);

  ConnectableDsl runIf(Predicate<UserSession> predicate, Spec spec);
}
