/**************************************************************************
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

package com.adobe.rhino.sdk.data;

import java.lang.reflect.Method;

/**
 * Scenario representation.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @version 1.0
 */
public class Scenario {

  /**
   * Method to be executed.
   */
  private final Method method;

  /**
   * Description of the scenario used in reports.
   */
  private final String description;

  public Scenario(final String description, final Method method) {
    this.method = method;
    this.description = description;
  }

  /**
   * @return The method instance.
   */
  public Method getMethod() {
    return method;
  }

  /**
   * @return The description.
   */
  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return "Scenario{" +
        "description='" + description + '\'' +
        '}';
  }
}