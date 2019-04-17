/* ************************************************************************
 *                                                                        *
 * ADOBE CONFIDENTIAL                                                     *
 * ___________________                                                    *
 *                                                                        *
 *  Copyright 2018 Adobe Systems Incorporated                             *
 *  All Rights Reserved.                                                  *
 *                                                                        *
 * NOTICE:  All information contained herein is, and remains              *
 * the property of Adobe Systems Incorporated and its suppliers,          *
 * if any.  The intellectual and technical concepts contained             *
 * herein are proprietary to Adobe Systems Incorporated and its           *
 * suppliers and are protected by trade secret or copyright law.          *
 * Dissemination of this information or reproduction of this material     *
 * is strictly forbidden unless prior written permission is obtained      *
 * from Adobe Systems Incorporated.                                       *
 **************************************************************************/

package com.adobe.rhino.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Applications executor. In fact, the application which leverage the SDK does not need this
 * method. This class is for testing purposes.
 *
 * @author Erhan Bagdemir
 * @since 1.0
 */
public class Application {

  private static final String CLASSPATH_RHINO_PROPERTIES = "classpath:///rhino.properties";
  private static final String BRANDING = "/branding.txt";
  private static final String SIMULATION = "Server-Status Simulation";

  public static void main(String... arg) {

    var simulation = new SimulationSpecImpl(CLASSPATH_RHINO_PROPERTIES, SIMULATION);

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
