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

package com.adobe.rhino.sdk.reporting;

/**
 * Default log formatter used in step log results.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 */
public class DefaultLogFormatter implements LogFormatter {

  @Override
  public String format(final LogEvent event) {
    return String.format("\"%s\"\t\"%s\"\t%s\t%s\t%s\t%s\n",
        event.scenario,
        event.start,
        event.elapsed,
        event.username,
        event.start
    );
  }
}
