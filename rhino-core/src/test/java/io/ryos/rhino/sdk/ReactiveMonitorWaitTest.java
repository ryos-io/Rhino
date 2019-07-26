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
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/*
 * Scenario:
 * First upload the content, the service returns 201 Created.
 * First attempt to monitor, and monitor API returns 404 Not Found.
 * Second attempt to monitor, and monitor API returns 200 OK.
 * At this point the cumulativeMeasurement on Spec makes the elapsed time be aggregated.
 */
@Ignore
public class ReactiveMonitorWaitTest {

  private static final String SIM_NAME = "Reactive Monitor Test";
  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().port(8089)
      .jettyAcceptors(2)
      .jettyAcceptQueueSize(100)
      .containerThreads(100));

  @Test
  public void testReactiveBasicHttp() {
    stubFor(WireMock.post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    stubFor(WireMock.put(urlEqualTo("/api/files"))
        .inScenario("retriable")
        .whenScenarioStateIs(STARTED)
        .willSetStateTo("monitor")
        .willReturn(aResponse()
            .withFixedDelay(100)
            .withStatus(201)));

    stubFor(WireMock.get(urlEqualTo("/api/monitor"))
        .inScenario("retriable")
        .whenScenarioStateIs("monitor")
        .willSetStateTo("monitor-2")
        .willReturn(aResponse()
            .withFixedDelay(100)
            .withStatus(404)));

    stubFor(WireMock.get(urlEqualTo("/api/monitor"))
        .inScenario("retriable")
        .whenScenarioStateIs("monitor-2")
        .willSetStateTo("ended")
        .willReturn(aResponse()
            .withFixedDelay(100)
            .withStatus(200)));

    Simulation.create(PROPERTIES_FILE, SIM_NAME).start();
  }
}
