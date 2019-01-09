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

package com.adobe.rhino.sdk;

/**
 * SimulationSpec runner controller. Within a performance testing project, it is likely that there are
 * multiple test classes. The controller runs each test case in an isolated execution context.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @version 1.0
 * @see SimulationRunner
 */
public interface SimulationSpec {

  /**
   * Execute all tests, detected by scanning the paths defined in the configuration.
   */
  void start();

  /**
   * Stop all running tests.
   */
  void stop();
}
