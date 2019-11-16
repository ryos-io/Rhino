package io.ryos.rhino.sdk.simulations;

import static io.ryos.rhino.sdk.dsl.specs.DSLSpec.http;
import static io.ryos.rhino.sdk.dsl.specs.HttpSpec.from;
import static io.ryos.rhino.sdk.dsl.specs.UploadStream.file;
import static io.ryos.rhino.sdk.dsl.specs.builder.SessionAccessor.session;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.providers.OAuthUserProvider;
import io.ryos.rhino.sdk.providers.UUIDProvider;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;

@Simulation(name = "Reactive Upload Test")
@Runner(clazz = ReactiveHttpSimulationRunner.class)
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class UploadLoadSimulation {

  private static final String FILES = "http://localhost:8089/api/files";
  private static final String X_REQUEST_ID = "X-Request-Id";
  private static final String X_API_KEY = "X-Api-Key";

  @UserProvider
  private OAuthUserProvider userProvider;

  @Provider(factory = UUIDProvider.class)
  private UUIDProvider uuidProvider;

  @Dsl(name = "Upload File")
  public LoadDsl testUploadAndGetFile() {
    return Start
        .dsl()
        .session("2. User", () -> userProvider.take())
        .run(http("PUT text.txt")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + uuidProvider.take()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(s -> FILES)
            .upload(() -> file("classpath:///test.txt"))
            .put()
            .saveTo("result"))
        .run(http("GET text.txt")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + uuidProvider.take()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth((session("2. User")))
            .endpoint(s -> FILES)
            .get());
  }
}
