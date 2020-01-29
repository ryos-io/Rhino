package io.ryos.rhino.sdk.simulations;

import static io.ryos.rhino.sdk.dsl.HttpDsl.from;
import static io.ryos.rhino.sdk.dsl.LoadDsl.dsl;
import static io.ryos.rhino.sdk.dsl.MaterializableDslItem.http;
import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.session;
import static io.ryos.rhino.sdk.utils.TestUtils.getEndpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.data.HttpResponse;
import io.ryos.rhino.sdk.dsl.data.builder.MapperBuilder;
import io.ryos.rhino.sdk.dsl.mat.HttpDslData;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import java.io.IOException;
import java.util.UUID;

@Simulation(name = "Reactive Multi-User Test")
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class DiscoverAndGetSimulation {
  private static final String DISCOVERY_ENDPOINT = getEndpoint("discovery");
  private static final String X_REQUEST_ID = "X-Request-Id";
  private static final String X_API_KEY = "X-Api-Key";
  private static final ObjectMapper MAPPER = new ObjectMapper();


  @Dsl(name = "Load DSL Discovery and GET")
  public LoadDsl singleTestDsl() {
    return dsl()
        .run(http("Discovery Request")
            .header(session -> from(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(DISCOVERY_ENDPOINT)
            .get()
            .saveTo("result"))
        .map(MapperBuilder.from("result")
            .doMap(result -> extractEndpoint((HttpDslData) result)).saveTo("endpoint"))
        .run(http("Get Request")
            .header(session -> from(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
            .header(X_API_KEY, SimulationConfig.getApiKey())
            .auth()
            .endpoint(session("endpoint"))
            .get());
  }

  private String extractEndpoint(HttpDslData result) {
    try {
      HttpResponse response = result.getResponse();
      JsonNode jsonNode = MAPPER.readTree(response.getResponse().getResponseBody());
      return jsonNode.get("endpoint").asText();
    } catch (IOException e) {
      e.printStackTrace();
    }

    throw new RuntimeException();
  }

}
