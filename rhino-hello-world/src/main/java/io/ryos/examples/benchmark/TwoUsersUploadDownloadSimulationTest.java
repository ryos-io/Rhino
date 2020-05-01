package io.ryos.examples.benchmark;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.ryos.examples.simulations.TwoUsersUploadDownloadSimulation;
import io.ryos.rhino.sdk.Simulation;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

public class TwoUsersUploadDownloadSimulationTest {

  private static final String PROPS = "classpath:///rhino.properties";
  private static final int PORT = 8089;

  public static void main(String... args) {

    var wireMockServer = new WireMockServer(PORT);
    wireMockServer.start();

    configureFor("localhost", 8089);
    stubFor(WireMock.get(urlEqualTo("/api/status")).willReturn(aResponse()
        .withStatus(200)
        .withFixedDelay(200)));

    stubFor(WireMock.get(urlMatching("/api/files/.*")).willReturn(aResponse()
        .withStatus(200)
        .withFixedDelay(100)));

    stubFor(WireMock.put(urlMatching("/api/files/.*")).willReturn(aResponse()
        .withStatus(201)
        .withFixedDelay(100)));

    stubFor(WireMock.post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    Simulation.getInstance(PROPS, TwoUsersUploadDownloadSimulation.class).start();

    // Stopping
    wireMockServer.stop();
  }
}
