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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 */
public class InjectionPoint<T extends Annotation> {

  private final Field field;
  private final T annotation;

  public InjectionPoint(
      final Field field,
      final T annotation) {

    this.field = field;
    this.annotation = annotation;
  }

  public Field getField() {
    return field;
  }

  public T getAnnotation() {
    return annotation;
  }
}
