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

import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.data.InjectionPoint;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

  private final SimulationMetadata simulationMetadata;
  private final List<InjectionPoint<Provider>> injectionPointList;

  public AbstractSimulationInjector(final SimulationMetadata simulationMetadata) {
    this.simulationMetadata = Objects.requireNonNull(simulationMetadata);
    this.injectionPointList = Arrays.stream(getSimulationMetadata().getSimulationClass().getDeclaredFields())
            .filter(hasFeeder)
            .map(injectionPointFunction)
            .collect(Collectors.toList());
  }

  // Predicate to search fields for Provider annotation.
  final Predicate<Field> hasFeeder = f -> Arrays
      .stream(f.getDeclaredAnnotations())
      .anyMatch(Provider.class::isInstance);

  final Function<Field, InjectionPoint<Provider>> injectionPointFunction =
      f -> new InjectionPoint<>(f,
          f.getDeclaredAnnotation(Provider.class));


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

  /* Find the first annotation type, clazzAnnotation, on field declarations of the clazz.  */
  protected void injectCustomFeeders(final Object simulationInstance) {
    getInjectionPointList().forEach(ip -> feed(simulationInstance, ip));
  }

  // Provider the feeder value into the field.
  protected void feed(final Object instance, final InjectionPoint<Provider> injectionPoint) {
    Objects.requireNonNull(instance, "Object instance is null.");
    var factoryInstance = instanceOf(injectionPoint.getAnnotation().clazz()).orElseThrow();
    try {
      var field = injectionPoint.getField();
      field.setAccessible(true);
      field.set(instance, factoryInstance);
    } catch (IllegalAccessException e) {
      LOG.error("Access to field failed.", e);
    } catch (IllegalArgumentException e) {
      LOG.error("Provider's return type and field's type is not compatible: " + e.getMessage());
    }
  }

  public SimulationMetadata getSimulationMetadata() {
    return simulationMetadata;
  }

  public List<InjectionPoint<Provider>> getInjectionPointList() {
    return injectionPointList;
  }
}
