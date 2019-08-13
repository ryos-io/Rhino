package io.ryos.rhino.sdk.simulations;

import static io.ryos.rhino.sdk.dsl.specs.HttpSpec.from;
import static io.ryos.rhino.sdk.dsl.specs.Spec.http;
import static io.ryos.rhino.sdk.dsl.specs.Spec.some;
import static io.ryos.rhino.sdk.dsl.specs.impl.ForEachBuilder.in;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.annotations.CleanUp;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Prepare;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.dsl.specs.Spec.Scope;
import io.ryos.rhino.sdk.dsl.specs.impl.MapperBuilder;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactory;
import java.util.UUID;
import org.asynchttpclient.Response;

@Simulation(name = "Reactive Test", durationInMins = 1)
@Runner(clazz = ReactiveHttpSimulationRunner.class)
@UserRepository(factory = OAuthUserRepositoryFactory.class)
public class ReactiveBasicHttpGetSimulation {

  private static final String FILES_ENDPOINT = "http://localhost:8089/api/files";
  private static final String X_REQUEST_ID = "X-Request-Id";
  private static final String X_API_KEY = "X-Api-Key";

  @Prepare
  public static LoadDsl prepare() {
    return Start.dsl()
        .run(http("Prepare")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(FILES_ENDPOINT)
            .get()
            .saveTo("result", Scope.SIMULATION))
        .run(some("Status").as(session -> {
          session.<Response>get("result")
              .ifPresent(httpResponse -> System.out.println(httpResponse.getStatusCode()));
          return "OK";
        }));
  }

  @CleanUp
  public static LoadDsl cleanUp() {
    return Start.dsl()
        .run(http("Clean-up")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(FILES_ENDPOINT)
            .get()
            .saveTo("result"))
        .run(some("Status").as((session) -> {
          session.<Response>get("result")
              .ifPresent(httpResponse -> System.out.println(httpResponse.getStatusCode()));
          return "OKs";
        }));
  }

  @Dsl(name = "Load DSL Request")
  public LoadDsl singleTestDsl() {
    return Start.dsl()
        .run(http("Files Request")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(FILES_ENDPOINT)
            .get()
            .saveTo("result"))
        .map(MapperBuilder.<Response, String>from("result")
            .doMap(s -> s.getStatusCode() + " returned."))
        .forEach(in("result").apply(o -> some("measurement")
            .as((session) -> {
              System.out.println(o);
              return "OK";
            }))
            .saveTo("result"));
  }
}
