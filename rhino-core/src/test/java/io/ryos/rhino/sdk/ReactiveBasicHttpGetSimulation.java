package io.ryos.rhino.sdk;

import static io.ryos.rhino.sdk.specs.HttpSpec.from;
import static io.ryos.rhino.sdk.specs.Spec.http;

import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;

@Simulation(name = "Reactive Test")
@Runner(clazz = ReactiveHttpSimulationRunner.class)
@UserRepository(max = 1, factory = OAuthUserRepositoryFactoryImpl.class)
public class ReactiveBasicHttpGetSimulation {

  private static final String DISCOVERY_ENDPOINT = "https://cc-api-storage-stage.adobe.io/";
  private static final String X_REQUEST_ID = "X-Request-Id";
  private static final String X_API_KEY = "X-Api-Key";

  @UserProvider
  private io.ryos.rhino.sdk.feeders.UserProvider userProvider;

  @Dsl(name = "Discovery")
  public LoadDsl singleTestDsl() {
    return Start
        .spec()
        .run(http("Discovery")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + userProvider.take()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(DISCOVERY_ENDPOINT)
            .get());
  }
}
