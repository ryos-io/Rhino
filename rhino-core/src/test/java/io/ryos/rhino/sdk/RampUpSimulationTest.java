package io.ryos.rhino.sdk;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.providers.OAuthUserProvider;
import io.ryos.rhino.sdk.simulations.RampUpSimulation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RampUpSimulationTest {
  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";
  private static final int PORT = 8089;
  private WireMockServer wmServer;

  @UserProvider
  private OAuthUserProvider userProvider;

  @Before
  public void setUp() {
    wmServer = new WireMockServer(wireMockConfig().port(PORT)
        .jettyAcceptors(2)
        .jettyAcceptQueueSize(100)
        .containerThreads(100));
    wmServer.start();
  }

  @After
  public void tearDown() {
    wmServer.stop();
  }

  @Test
  public void test() {
    WireMock.configureFor("localhost", PORT);

    wmServer.stubFor(WireMock.post(urlEqualTo("/token"))
        .willReturn(aResponse().withStatus(200)
            .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    Simulation.getInstance(PROPERTIES_FILE, RampUpSimulation.class).start();
  }
}
