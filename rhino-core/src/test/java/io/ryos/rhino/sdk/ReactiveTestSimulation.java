package io.ryos.rhino.sdk;

import static io.ryos.rhino.sdk.specs.HttpSpec.from;
import static io.ryos.rhino.sdk.specs.Spec.http;

import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserFeeder;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.feeders.UserProvider;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.users.data.OAuthUser;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import java.util.List;
import java.util.Map;

@Simulation(name = "Reactive Test")
@Runner(clazz = ReactiveHttpSimulationRunner.class)
public class ReactiveTestSimulation {

  private static final String DISCOVERY_ENDPOINT = "https://cc-api-storage-stage.adobe.io/";
  private static final String X_REQUEST_ID = "X-Request-Id";
  public static final String X_API_KEY = "X-Api-Key";

  @UserFeeder(max = 1, factory = OAuthUserRepositoryFactoryImpl.class)
  private UserProvider provider;

  @Dsl(name = "Discovery")
  public LoadDsl singleTestDsl() {
    return Start
        .spec()
        .run(http("Discovery")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + provider.take()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(DISCOVERY_ENDPOINT)
            .get());
  }
}
