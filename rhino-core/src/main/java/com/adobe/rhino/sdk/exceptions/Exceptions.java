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

package com.adobe.rhino.sdk.exceptions;

import java.lang.reflect.Constructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Exception utils.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @since 1.0
 */
public class Exceptions {

  private static final Logger LOG = LogManager.getLogger(Exceptions.class);

  /**
   * Rethrow any instance of {@link RuntimeException} by wrapping it into a new one, of which type
   * passed as method parameter while logging the error out.
   *
   * @param e Exception instance, that is caught.
   * @param exception The new exception to be rethrown.
   * @param logMsg Log message.
   */
  public static void rethrow(Exception e, Class<? extends RuntimeException> exception,
      String logMsg) {
    if (logMsg != null) {
      LOG.error(logMsg, e);
    }
    final Constructor<? extends Exception> declaredConstructor;
    try {
      declaredConstructor = exception.getDeclaredConstructor(Throwable.class);
      throw declaredConstructor.newInstance(e);
    } catch (Exception e1) {
      LOG.error(e1);
    }
  }

  /**
   * Rethrow any instance of {@link RuntimeException} by wrapping it into a new one, of which type
   * passed as method parameter.
   *
   * @param e Exception instance, that is caught.
   * @param exception The new exception to be rethrown.
   */
  public static void rethrow(Exception e, Class<? extends RuntimeException> exception) {
    rethrow(e, exception, null);
  }
}
