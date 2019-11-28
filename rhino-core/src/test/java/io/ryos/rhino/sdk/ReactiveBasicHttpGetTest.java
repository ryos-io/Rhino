package io.ryos.rhino.sdk;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.ryos.rhino.sdk.simulations.ReactiveBasicHttpGetSimulation;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore
public class ReactiveBasicHttpGetTest {

  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";
  private static final String AUTH_ENDPOINT = "test.oauth2.endpoint";
  private static final String WIREMOCK_PORT = "wiremock.port";

  @Rule
  public WireMockRule wmServer = new WireMockRule(wireMockConfig().port(8089)
      .jettyAcceptors(2)
      .jettyAcceptQueueSize(100)
      .containerThreads(100));

  @Test
  public void testReactiveBasicHttpGet() throws InterruptedException {
    WireMock.configureFor("localhost", 8089);

    wmServer.stubFor(WireMock.post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    wmServer.stubFor(WireMock.get(urlEqualTo("/api/files"))
        .willReturn(aResponse()
            .withFixedDelay(800)
            .withStatus(200)));

    wmServer.stubFor(WireMock.get(urlEqualTo("/api/prepare"))
        .willReturn(aResponse()
            .withFixedDelay(800)
            .withStatus(200)));

    System.setProperty(AUTH_ENDPOINT, "http://localhost:" + 8089 + "/token");
    System.setProperty(WIREMOCK_PORT, Integer.toString(8089));

    Simulation.getInstance(PROPERTIES_FILE, ReactiveBasicHttpGetSimulation.class).start();
    Thread.sleep(5000L);
  }
}
