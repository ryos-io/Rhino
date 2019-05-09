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

package io.ryos.rhino.sdk.utils;

import io.ryos.rhino.sdk.data.Pair;
import io.ryos.rhino.sdk.exceptions.Exceptions;
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
 * @author <a href="mailto:erhan@ryos.io">Erhan Bagdemir</a>
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
