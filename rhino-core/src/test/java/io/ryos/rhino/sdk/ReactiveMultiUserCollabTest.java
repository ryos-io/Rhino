/*
 * Copyright 2019 Ryos.io.
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
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.ryos.rhino.sdk.simulations.ReactiveMultiUserCollabSimulation;
import io.ryos.rhino.sdk.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ReactiveMultiUserCollabTest {

  private static final String PROPERTIES_FILE = "classpath:///rhino.properties";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
    wireMockRule.stop();
  }

  @Test
  public void testMultiUser() throws InterruptedException {

    wireMockRule.stubFor(WireMock.post(urlEqualTo("/token"))
        .inScenario("multiUser")
        .willSetStateTo("secondUser")
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"access_token\": \"abc123\", \"refresh_token\": \"abc123\"}")));

    wireMockRule.stubFor(WireMock.post(urlEqualTo("/token"))
        .inScenario("multiUser")
        .whenScenarioStateIs("secondUser")
        .willSetStateTo("end")
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"access_token\": \"abc345\", \"refresh_token\": \"abc345\"}")));

    wireMockRule.stubFor(WireMock.put(urlEqualTo("/api/files/file1"))
        .willReturn(aResponse()
            .withStatus(201)
            .withFixedDelay(400)));

    wireMockRule.stubFor(WireMock.put(urlEqualTo("/api/files/file2"))
        .willReturn(aResponse()
            .withStatus(201)
            .withFixedDelay(400)));

    wireMockRule.stubFor(WireMock.put(urlEqualTo("/api/files"))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("Location",
                "http://localhost:" + wireMockRule.port() + "/api/files/newAsset")
            .withFixedDelay(400)));

    wireMockRule.stubFor(WireMock.get(urlEqualTo("/api/files/newAsset"))
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(400)));

    wireMockRule.stubFor(WireMock.get(urlEqualTo("/api/files"))
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(400)));

    TestUtils.overridePorts(wireMockRule.port());

    Simulation.getInstance(PROPERTIES_FILE, ReactiveMultiUserCollabSimulation.class).times(1);

    Thread.sleep(3000);

    wireMockRule.verify(3, putRequestedFor(urlEqualTo("/api/files")));
    wireMockRule.verify(1, getRequestedFor(urlEqualTo("/api/files/newAsset")));
    wireMockRule.verify(2, getRequestedFor(urlEqualTo("/api/files")));
    wireMockRule.verify(4, putRequestedFor(urlMatching("/api/files/.*")));
  }
}
