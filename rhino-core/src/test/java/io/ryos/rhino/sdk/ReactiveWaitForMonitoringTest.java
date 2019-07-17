/*
 * Copyright 2018 Ryos.io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ryos.rhino.sdk;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.Rule;
import org.junit.Test;

/**
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class ReactiveWaitForMonitoringTest {

  private static final String SIM_NAME = "Reactive Async Wait Test";
  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().port(8089)
      .jettyAcceptors(4)
      .jettyAcceptQueueSize(0)
      .containerThreads(150));

  @Test
  public void testReactiveBasicHttp() {
    stubFor(WireMock.post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    stubFor(WireMock.put(urlEqualTo("/api/files"))
        .willReturn(aResponse()
            .withFixedDelay(800)
            .withStatus(201)));

    stubFor(WireMock.get(urlEqualTo("/api/monitor"))
        .willReturn(aResponse()
            .withFixedDelay(50)
            .withStatus(201)));

    stubFor(WireMock.get(urlEqualTo("/api/monitor"))
        .inScenario("monitor")
        .whenScenarioStateIs(Scenario.STARTED)
        .willSetStateTo("Second Attempt")
        .willReturn(aResponse()
            .withFixedDelay(50)
            .withStatus(201)));

    stubFor(WireMock.get(urlEqualTo("/api/monitor"))
        .inScenario("monitor")
        .whenScenarioStateIs("Second Attempt")
        .willSetStateTo(Scenario.STARTED)
        .willReturn(aResponse()
            .withFixedDelay(50)
            .withStatus(200)));

    Simulation.create(PROPERTIES_FILE, SIM_NAME).start();
  }
}
