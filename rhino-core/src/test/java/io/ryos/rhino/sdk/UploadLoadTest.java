package io.ryos.rhino.sdk;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.ryos.rhino.sdk.simulations.UploadLoadSimulation;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore
public class UploadLoadTest {

  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8089);

  @Test
  public void testUploadLoad() {

    stubFor(WireMock.post(urlEqualTo("/token"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    stubFor(WireMock.put(urlEqualTo("/api/files"))
            .willReturn(aResponse()
                .withStatus(201).withFixedDelay(200)));

    stubFor(WireMock.get(urlEqualTo("/api/files"))
        .willReturn(aResponse()
            .withStatus(200).withFixedDelay(200)));

    Simulation.create(PROPERTIES_FILE, UploadLoadSimulation.class).start();
  }
}
