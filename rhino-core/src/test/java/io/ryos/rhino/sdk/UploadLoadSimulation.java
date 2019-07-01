package io.ryos.rhino.sdk;

import static io.ryos.rhino.sdk.specs.HttpSpec.from;
import static io.ryos.rhino.sdk.specs.Spec.http;

import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.feeders.OAuthUserProvider;
import io.ryos.rhino.sdk.feeders.UUIDProvider;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import java.io.InputStream;

@Simulation(name = "Reactive Upload Test")
@Runner(clazz = ReactiveHttpSimulationRunner.class)
@UserRepository(max = 1, factory = OAuthUserRepositoryFactoryImpl.class)
public class UploadLoadSimulation {

  private static final String DISCOVERY_ENDPOINT = "https://localhost/files";
  private static final String X_REQUEST_ID = "X-Request-Id";
  private static final String X_API_KEY = "X-Api-Key";

  @UserProvider
  private OAuthUserProvider userProvider;

  @Provider(factory = UUIDProvider.class)
  private UUIDProvider uuidProvider;

  @Dsl(name = "Upload File")
  public LoadDsl singleTestDsl() {
    return Start
        .spec()
        .run(http("text.txt")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + userProvider.take()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint((c) -> DISCOVERY_ENDPOINT + uuidProvider.take())
            .upload(this::getStream)
            .put());
  }

  private InputStream getStream() {

    final InputStream resourceAsStream = getClass().getResourceAsStream("/test.txt");
    assert resourceAsStream != null;
    return resourceAsStream;
  }
}
