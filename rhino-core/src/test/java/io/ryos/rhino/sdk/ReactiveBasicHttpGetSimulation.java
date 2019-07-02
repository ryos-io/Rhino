package io.ryos.rhino.sdk;

import static io.ryos.rhino.sdk.specs.HttpSpec.from;
import static io.ryos.rhino.sdk.specs.Spec.http;
import static io.ryos.rhino.sdk.specs.Spec.some;

import io.ryos.rhino.sdk.annotations.CleanUp;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Prepare;
import io.ryos.rhino.sdk.annotations.RampUp;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.feeders.OAuthUserProvider;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactory;

@Simulation(name = "Reactive Test", durationInMins = 5)
@Runner(clazz = ReactiveHttpSimulationRunner.class)
@UserRepository(factory = OAuthUserRepositoryFactory.class)
@RampUp(startRps = 10, targetRps = 2000, duration = 1)
public class ReactiveBasicHttpGetSimulation {

  private static final String DISCOVERY_ENDPOINT = "http://localhost:8089/api/files";
  private static final String X_REQUEST_ID = "X-Request-Id";
  private static final String X_API_KEY = "X-Api-Key";

  @UserProvider
  private OAuthUserProvider userProvider;

  @Dsl(name = "Discovery")
  public LoadDsl singleTestDsl() {
    return Start.spec()
        .run(http("Discovery")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + userProvider.take()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(DISCOVERY_ENDPOINT)
            .get())
        .run(some("Output").as((u,m) -> {
          return u;
        }));
  }

  @Prepare
  public void prepare() {
    System.out.println("Preparation in progress.");
  }

  @CleanUp
  public void cleanUp() {
    System.out.println("Clean-up in progress.");
  }
}
