package io.ryos.rhino.sdk;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.ryos.rhino.sdk.simulations.DiscoverAndGetSimulation;
import io.ryos.rhino.sdk.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DiscoverAndGetSimulationTest {
  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  @After
  public void tearDown() {
    wireMockRule.stop();
  }

  @Test
  public void loadTestDiscoverAndGet() throws InterruptedException {
    TestUtils.overridePorts(wireMockRule.port());

    wireMockRule.stubFor(WireMock.post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    wireMockRule.stubFor(WireMock.get(urlEqualTo("/api/resource"))
        .willReturn(aResponse()
            .withFixedDelay(200)
            .withStatus(200)));

    wireMockRule.stubFor(WireMock.get(urlEqualTo("/api/discovery"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"endpoint\": \"http://localhost:"+wireMockRule.port()+"/api/resource\"}")));

    Simulation.getInstance(PROPERTIES_FILE, DiscoverAndGetSimulation.class).times(1);

    Thread.sleep(5000L);

    wireMockRule.verify(1, getRequestedFor(urlEqualTo("/api/resource")));
    wireMockRule.verify(1, getRequestedFor(urlEqualTo("/api/discovery")));
  }
}
