package io.ryos.rhino.sdk;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.ryos.rhino.sdk.simulations.ReactiveBasicHttpGetSimulation;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore
public class ReactiveBasicHttpGetTest {

  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().port(8089)
      .jettyAcceptors(2)
      .jettyAcceptQueueSize(100)
      .containerThreads(100));

  @Test
  public void testReactiveBasicHttpGet() throws InterruptedException {

    stubFor(WireMock.post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    stubFor(WireMock.get(urlEqualTo("/api/files"))
        .willReturn(aResponse()
            .withFixedDelay(800)
            .withStatus(200)));

    stubFor(WireMock.get(urlEqualTo("/api/prepare"))
        .willReturn(aResponse()
            .withFixedDelay(800)
            .withStatus(200)));

    Simulation.getInstance(PROPERTIES_FILE, ReactiveBasicHttpGetSimulation.class).start();
  }
}
