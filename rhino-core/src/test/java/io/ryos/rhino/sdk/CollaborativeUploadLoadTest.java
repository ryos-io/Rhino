package io.ryos.rhino.sdk;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.ryos.rhino.sdk.simulations.CollaborativeUploadLoadSimulation;
import io.ryos.rhino.sdk.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class CollaborativeUploadLoadTest {
  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  @After
  public void tearDown() {
    wireMockRule.stop();
  }

  @Test
  public void testUploadLoad() throws InterruptedException {
    stubFor(WireMock.post(urlEqualTo("/token"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    stubFor(WireMock.put(urlEqualTo("/api/files"))
            .willReturn(aResponse()
                .withStatus(201).withFixedDelay(100)));

    stubFor(WireMock.get(urlEqualTo("/api/files"))
        .willReturn(aResponse()
            .withStatus(200).withFixedDelay(10)));

    TestUtils.overridePorts(wireMockRule.port());

    Simulation.getInstance(PROPERTIES_FILE, CollaborativeUploadLoadSimulation.class).times(1);
    Thread.sleep(1000L);
    wireMockRule.verify(1, putRequestedFor(urlEqualTo("/api/files")));
    wireMockRule.verify(1, getRequestedFor(urlEqualTo("/api/files")));
  }
}
