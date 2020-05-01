/*
 * Copyright 2020 Ryos.io.
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

package io.ryos.examples.benchmark;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.ryos.examples.simulations.ExtractInformationWithMapSimulation;
import io.ryos.rhino.sdk.Simulation;
import org.apache.commons.math3.stat.inference.TestUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class ExtractInformationWithMapTest {

  private static final String PROPS = "classpath:///rhino.properties";
  private static final int PORT = 8089;

  public static void main(String... args) throws InterruptedException {
    var wmServer = startServer();

    wmServer.stubFor(WireMock.post(urlEqualTo("/token"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    wmServer.stubFor(WireMock.get(urlEqualTo("/api/resource"))
        .willReturn(aResponse()
            .withFixedDelay(200)
            .withStatus(200)));

    wmServer.stubFor(WireMock.get(urlEqualTo("/api/discovery"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"endpoint\": \"http://localhost:"+PORT+"/api/resource\"}")));

    Simulation.getInstance(PROPS, ExtractInformationWithMapSimulation.class).start();

    Thread.sleep(5000L);
  }

  private static WireMockServer startServer() {
    var wmServer = new WireMockServer(PORT);

    wmServer = new WireMockServer(wireMockConfig().port(PORT)
        .jettyAcceptors(2)
        .jettyAcceptQueueSize(100)
        .containerThreads(100));
    wmServer.start();

    WireMock.configureFor("localhost", PORT);

    return wmServer;
  }
}
