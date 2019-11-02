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
import io.ryos.rhino.sdk.exceptions.BadInjectionException;
import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import io.ryos.rhino.sdk.exceptions.IllegalMethodSignatureException;
import io.ryos.rhino.sdk.exceptions.RhinoFrameworkError;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reflection utility methods.
 *
 * @author Erhan Bagdemir
 */
public class ReflectionUtils {

  private static final Logger LOG = LogManager.getLogger(ReflectionUtils.class);

  private ReflectionUtils() {
  }

  public static Object enhanceInstanceAt(Field field) {
    var enhancer = new Enhancer();
    enhancer.setSuperclass(field.getType());
    enhancer.setCallback((FixedValue) () -> {
      throw new BadInjectionException("RH40 - Injection point is not ready. This happens when "
          + "your code tries to access the providers before they are initialized.\n"
          + field.toString() + "\n"
          + "In Job DSL access to providers at injection points must be within the lambdas so "
          + "that the access occurs lazy. \nPlease refer to documentation regarding this: \n"
          + "https://github.com/ryos-io/Rhino/wiki/FAQ");
    });
    return enhancer.create();
  }

  public static <T> void setValueAtInjectionPoint(final T object, final Field f,
      final Object simulationInstance) {
    try {
      f.setAccessible(true);
      f.set(simulationInstance, object);
    } catch (IllegalAccessException e) {
      LOG.error(e);
      //TODO
    }
  }

  /* Find the first annotation type, clazzAnnotation, on field declarations of the clazz.  */
  public static <T extends Annotation> Pair<Optional<Field>, T> findFieldByAnnotation(
      final Class clazz,
      final Class<T> clazzA) {

    var field = Arrays.stream(clazz.getDeclaredFields())
        .filter(m -> Arrays.stream(m.getDeclaredAnnotations())
            .anyMatch(clazzA::isInstance))
        .findFirst();

    return new Pair<>(field,
        field.map(f -> f.getDeclaredAnnotationsByType(clazzA)[0])
            .orElseThrow());
  }

  /**
   * get the list of annotations along with {@link Field} instances.
   */
  public static <T extends Annotation> List<Pair<Field, T>> getFieldsByAnnotation(
      final Class clazz,
      final Class<T> clzAnnotation) {

    var fields = Arrays.stream(clazz.getDeclaredFields())
        .filter(m -> Arrays.stream(m.getDeclaredAnnotations()).anyMatch(clzAnnotation::isInstance))
        .collect(Collectors.toList());

    return fields.stream().map(f ->
        new Pair<>(f, f.getDeclaredAnnotationsByType(clzAnnotation)[0]))
        .collect(Collectors.toList());
  }

  public static <T extends Annotation> Optional<T> getClassLevelAnnotation(final Class clazz,
      final Class<T> annotation) {
    return Optional.ofNullable((T) clazz.getDeclaredAnnotation(annotation));
  }

  static Optional<Constructor> getDefaultConstructor(final Class clazz) {
    try {
      return Optional.of(clazz.getConstructor());
    } catch (NoSuchMethodException e) {
      ExceptionUtils.rethrow(e, RuntimeException.class);
      LOG.error(e);
    }
    return Optional.empty();
  }

  public static <T> Optional<T> instanceOf(final Class<T> clazz) {
    var defaultConstructor = getDefaultConstructor(clazz).orElseThrow();
    try {
      return Optional.of((T) defaultConstructor.newInstance());
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      LOG.error(e);
    }
    return Optional.empty();
  }

  public static <T> T executeMethod(Method method, Object declaring, Object... args) {
    Objects.requireNonNull(method);

    try {
      return (T) method.invoke(declaring, args);
    } catch (IllegalAccessException | InvocationTargetException e) {
      LOG.error(e.getCause().getMessage());
      throw new RhinoFrameworkError(
          "Cannot invoke the method with step: " + method.getName() + "()", e);
    }
  }

  public static <T> T executeStaticMethod(Method method, Object... args) {
    Objects.requireNonNull(method);

    try {
      return (T) method.invoke(null, args);
    } catch (NullPointerException npe) {
      throw new IllegalMethodSignatureException(
          "@Prepare/@CleanUp annotation must be used on public static methods.");
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RhinoFrameworkError(
          "Cannot invoke the method with step: " + method.getName() + "()", e);
    }
  }
}
