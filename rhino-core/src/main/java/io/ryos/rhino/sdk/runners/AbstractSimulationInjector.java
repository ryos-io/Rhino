/*
 * Copyright 2018 Ryos.io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ryos.rhino.sdk.runners;

import static io.ryos.rhino.sdk.utils.ReflectionUtils.instanceOf;

import io.ryos.rhino.sdk.annotations.Feeder;
import io.ryos.rhino.sdk.data.InjectionPoint;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract injector includes common methods.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public abstract class AbstractSimulationInjector implements SimulationInjector {

  private static final Logger LOG = LogManager.getLogger(AbstractSimulationInjector.class);

  // Predicate to search fields for Feedable annotation.
  final Predicate<Field> hasFeeder = f -> Arrays
      .stream(f.getDeclaredAnnotations())
      .anyMatch(io.ryos.rhino.sdk.annotations.Feeder.class::isInstance);

  final Function<Field, InjectionPoint<Feeder>> injectionPointFunction =
      f -> new InjectionPoint<>(f,
          f.getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.Feeder.class));


  <T> void setValueToInjectionPoint(final T object, final Field f,
      final Object simulationInstance) {
    try {
      f.setAccessible(true);
      f.set(simulationInstance, object);
    } catch (IllegalAccessException e) {
      LOG.error(e);
      //TODO
    }
  }

  // Feedable the feeder value into the field.
  protected void feed(final Object instance, final InjectionPoint<Feeder> injectionPoint) {

    Objects.requireNonNull(instance, "Object instance is null.");
    var factoryInstance = instanceOf(injectionPoint.getAnnotation().factory()).orElseThrow();
    var value = factoryInstance.take();
    try {
      var field = injectionPoint.getField();
      field.setAccessible(true);
      //TODO pre-check before assignment.
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      LOG.error("Access to field failed.", e);
    } catch (IllegalArgumentException e) {
      LOG.error("Feedable's return type and field's type is not compatible: " + e.getMessage());
    }
  }
}
