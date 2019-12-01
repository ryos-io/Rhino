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

package io.ryos.rhino.sdk.utils;

public class TestUtils {

  private static final String AUTH_ENDPOINT = "test.oauth2.endpoint";
  private static final String WIREMOCK_PORT = "wiremock.port";

  public static void overridePorts(int port) {
    System.setProperty(AUTH_ENDPOINT, "http://localhost:" + port + "/token");
    System.setProperty(WIREMOCK_PORT, Integer.toString(port));
  }
}
