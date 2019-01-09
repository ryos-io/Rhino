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

package com.adobe.rhino.sdk.annotations;

import com.adobe.rhino.sdk.reporting.DefaultLogFormatter;
import com.adobe.rhino.sdk.reporting.LogFormatter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Class annotation to provide metadata about logging strategy. The benchmark metrics will be
 * written into the log file where this annotation points.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Logging {

  /**
   * The path where benchmark metrics should be written.
   *
   * @return The path where to log.
   */
  String file();

  /**
   * Log formatter.
   *
   * @return Log formatter instance.
   */
  Class<? extends LogFormatter> formatter() default DefaultLogFormatter.class;
}
