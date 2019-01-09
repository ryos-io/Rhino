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

package com.adobe.rhino.sdk.utils;

import com.adobe.rhino.sdk.data.Pair;
import com.adobe.rhino.sdk.exceptions.Exceptions;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Reflection utility methods.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 */
public class ReflectionUtils {

  private static final Logger LOG = LogManager.getLogger(ReflectionUtils.class);

  /* Find the first annotation type, clazzAnnotation, on field declarations of the clazz.  */
  public static <T extends Annotation> Pair<Optional<Field>, T> findFieldByAnnotation(Class clazz,
      Class<T> clazzA) {

    final Optional<Field> field = Arrays.stream(clazz.getDeclaredFields())
        .filter(m -> Arrays.stream(m.getDeclaredAnnotations()).anyMatch(clazzA::isInstance))
        .findFirst();

    return new Pair<>(field,
        field.map(f -> f.getDeclaredAnnotationsByType(clazzA)[0])
            .orElseThrow());
  }

  // Find the first annotation type, clazzAnnotation, on field declarations of the clazz.
  public static <T extends Annotation> Optional<Pair<Field, T>> getFieldByAnnotation(Class clazz,
      Class<T> clzAnnotation) {

    Optional<Field> field = Arrays.stream(clazz.getDeclaredFields())
        .filter(m -> Arrays.stream(m.getDeclaredAnnotations()).anyMatch(clzAnnotation::isInstance))
        .findFirst();

    return field.map(fld -> new Pair<>(fld, fld.getDeclaredAnnotationsByType(clzAnnotation)[0]));
  }

  public static <T extends Annotation> Optional<T> getClassLevelAnnotation(final Class clazz,
      final Class<T> annotation) {
    return Optional.ofNullable((T) clazz.getDeclaredAnnotation(annotation));
  }

  static Optional<Constructor> getDefaultConstructor(final Class clazz) {
    try {
      return Optional.of(clazz.getConstructor());
    } catch (NoSuchMethodException e) {
      Exceptions.rethrow(e, RuntimeException.class);
      LOG.error(e);
    }
    return Optional.empty();
  }

  public static <T> Optional<T> instanceOf(Class<T> clazz) {
    var defaultConstructor = getDefaultConstructor(clazz).orElseThrow();
    try {
      return Optional.of((T) defaultConstructor.newInstance());
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      LOG.error(e);
    }
    return Optional.empty();
  }
}
