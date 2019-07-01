/*
  Copyright 2018 Ryos.io.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package io.ryos.rhino.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Applications executor. In fact, the application which leverage the SDK does not need this
 * method. This class is for testing purposes.
 *
 * @author Erhan Bagdemir
 * @since 1.0.0
 */
public class Application {

  private static final String CLASSPATH_RHINO_PROPERTIES = "classpath:///rhino.properties";
  private static final String BRANDING = "/branding.txt";
  private static final String SIMULATION = "Server-Status Simulation";

  public static void main(String... arg) {

    var simulation = new SimulationImpl(CLASSPATH_RHINO_PROPERTIES, SIMULATION);

    simulation.start();
  }

  // Shows up the Rhino branding.
  static void showBranding() {

    final InputStream stream = Application.class.getResourceAsStream(BRANDING);
    try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream))) {
      while (bufferedReader.ready()) {
        System.out.println(bufferedReader.readLine());
      }
    } catch (IOException e) {
      System.err.println("Can not start the application:" + e.getMessage());
    }
  }
}
