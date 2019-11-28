package io.ryos.rhino.sdk;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.ryos.rhino.sdk.simulations.DSLRunUntilSimulation;
import org.junit.Rule;
import org.junit.Test;

public class DSLRunUntilSimulationTest {

  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";
  private static final String FAILED = "Failed";
  private static final String AUTH_ENDPOINT = "test.oauth2.endpoint";
  private static final String WIREMOCK_PORT = "wiremock.port";

  @Rule
  public WireMockRule wmServer = new WireMockRule(wireMockConfig().port(8088)
      .jettyAcceptors(2)
      .jettyAcceptQueueSize(100)
      .containerThreads(100));

  @Test
  public void testFirstAttemptFailingAndRetryUntil() throws InterruptedException {
    WireMock.configureFor("localhost", 8088);

    wmServer.stubFor(WireMock.post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    wmServer.stubFor(WireMock.get(urlEqualTo("/api/files"))
        .willReturn(aResponse()
            .withFixedDelay(1000)
            .withStatus(200)));

    wmServer.stubFor(WireMock.put(urlEqualTo("/api/files"))
        .inScenario("1st POST failing")
        .whenScenarioStateIs(STARTED)
        .willSetStateTo(FAILED)
        .willReturn(aResponse()
            .withFixedDelay(1000)
            .withStatus(409)));

    wmServer.stubFor(WireMock.put(urlEqualTo("/api/files"))
        .inScenario("1st POST failing")
        .whenScenarioStateIs(FAILED)
        .willSetStateTo(STARTED)
        .willReturn(aResponse()
            .withFixedDelay(1000)
            .withStatus(200)));

    wmServer.stubFor(WireMock.get(urlEqualTo("/api/notreachable"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    System.setProperty(AUTH_ENDPOINT, "http://localhost:" + 8088 + "/token");
    System.setProperty(WIREMOCK_PORT, Integer.toString(8088));

    System.out.println(wmServer.isRunning());
    Simulation.getInstance(PROPERTIES_FILE, DSLRunUntilSimulation.class).start();

    Thread.sleep(5000L);
  }
}
