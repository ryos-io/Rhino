package io.ryos.rhino.sdk;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.ryos.rhino.sdk.simulations.UploadLoadSimulation;
import io.ryos.rhino.sdk.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class UploadLoadTest {
  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";
  private static final int PORT = 8087;

  private WireMockServer wmServer;

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
  public void testUploadLoad() throws InterruptedException {
    WireMock.configureFor("localhost", PORT);

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

    TestUtils.overridePorts(PORT);

    Simulation.getInstance(PROPERTIES_FILE, UploadLoadSimulation.class).start();

    Thread.sleep(1000L);
  }
}
