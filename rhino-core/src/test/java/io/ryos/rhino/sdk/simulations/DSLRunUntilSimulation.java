package io.ryos.rhino.sdk.simulations;

import static io.ryos.rhino.sdk.dsl.specs.DSLSpec.http;
import static io.ryos.rhino.sdk.dsl.specs.HttpSpec.from;
import static io.ryos.rhino.sdk.dsl.specs.UploadStream.file;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.Runner;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.dsl.LoadDsl;
import io.ryos.rhino.sdk.dsl.Start;
import io.ryos.rhino.sdk.dsl.mat.HttpSpecData;
import io.ryos.rhino.sdk.providers.UUIDProvider;
import io.ryos.rhino.sdk.runners.ReactiveHttpSimulationRunner;
import java.util.UUID;

@Simulation(name = "DSLRunUntilSimulation")
@Runner(clazz = ReactiveHttpSimulationRunner.class)
public class DSLRunUntilSimulation {
    private static final String FILES_ENDPOINT = "http://localhost:8089/api/files";
    private static final String MONITOR_ENDPOINT = "http://localhost:8089/api/monitor";
    private static final String X_REQUEST_ID = "X-Request-Id";
    private static final String X_API_KEY = "X-Api-Key";

    @Provider(factory = UUIDProvider.class)
    private UUIDProvider uuidProvider;

    @Dsl(name = "Upload File")
    public LoadDsl singleTestDsl() {
        return Start
                .dsl()
            .runUntil(s ->
                    s.<HttpSpecData>get("result").map(d -> d.getResponse().getStatusCode()).orElse(-1)
                        == 200,
                        http("PUT Request")
                        .header(c -> from(X_REQUEST_ID, "Rhino-" + uuidProvider.take()))
                        .header(X_API_KEY, SimulationConfig.getApiKey())
                        .auth()
                        .upload(() -> file("classpath:///test.txt"))
                        .endpoint((c) -> FILES_ENDPOINT)
                        .put()
                        .saveTo("result"))
                .run(http("GET on Files")
                        .header(c -> from(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
                        .header(X_API_KEY, SimulationConfig.getApiKey())
                        .auth()
                        .endpoint(FILES_ENDPOINT)
                        .get()
                        .saveTo("result2"));
    }
}
