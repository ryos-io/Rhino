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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marker annotation to activate Influx DB writer.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @since 1.1.4
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Influx {

}
