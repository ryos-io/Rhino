package io.ryos.rhino.sdk;

import static io.ryos.rhino.sdk.specs.Spec.http;

import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Influx;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserFeeder;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.users.repositories.UserRepository;
import io.ryos.rhino.sdk.users.repositories.UserRepositoryFactory;

@Simulation(name = "Reactive Test")
@Influx
@Runner(clazz = ReactiveHttpSimulationRunner.class)
public class ReactiveTestSimulation {

  @UserFeeder(factory = UserRepositoryFactory.class)
  private UserRepository<UserSession> userRepository;

  @Dsl(name = "Google.com")
  public LoadDsl singleTestDsl() {
    return Start
        .spec()
        .run(http("HEAD")
            .endpoint((r) -> "http://google.com")
            .header("X-Request-ID", "123" + userRepository.take().getUser().getUsername())
            .head());
  }

  @Dsl(name = "Mixture")
  public LoadDsl multipleTestDsl() {
    return Start
        .spec()
        .run(http("Google.com GET")
                .endpoint((r) -> "http://google.com")
                .header("X-Request-ID", "123")
                .get())
        .run(http("Microsoft.com HEAD")
            .endpoint((r) -> "http://microsoft.com")
            .head());
  }
}
