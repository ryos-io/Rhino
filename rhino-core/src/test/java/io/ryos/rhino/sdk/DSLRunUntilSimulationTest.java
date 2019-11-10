package io.ryos.rhino.sdk;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.ryos.rhino.sdk.simulations.DSLRunUntilSimulation;
import org.junit.Rule;
import org.junit.Test;

public class DSLRunUntilSimulationTest {

    private static final String SIM_NAME = "DSLRunUntilSimulation";
    private static final String PROPERTIES_FILE = "classpath:///rhino.properties";
    private static final String FAILED = "Failed";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8089)
            .jettyAcceptors(2)
            .jettyAcceptQueueSize(100)
            .containerThreads(100));

    @Test
    public void testFirstAttemptFailingAndRetryUntil() throws InterruptedException {

        stubFor(WireMock.post(urlEqualTo("/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

        stubFor(WireMock.get(urlEqualTo("/api/files"))
                .willReturn(aResponse()
                        .withFixedDelay(1000)
                        .withStatus(200)));

        stubFor(WireMock.put(urlEqualTo("/api/files"))
                .inScenario("1st POST failing")
                .whenScenarioStateIs(STARTED)
                .willSetStateTo(FAILED)
                .willReturn(aResponse()
                        .withFixedDelay(1000)
                        .withStatus(409)));

        stubFor(WireMock.put(urlEqualTo("/api/files"))
                .inScenario("1st POST failing")
                .whenScenarioStateIs(FAILED)
                .willSetStateTo(STARTED)
                .willReturn(aResponse()
                        .withFixedDelay(1000)
                        .withStatus(200)));

        stubFor(WireMock.get(urlEqualTo("/api/notreachable"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

      Simulation.getInstance(PROPERTIES_FILE, DSLRunUntilSimulation.class).start();
    }
}
