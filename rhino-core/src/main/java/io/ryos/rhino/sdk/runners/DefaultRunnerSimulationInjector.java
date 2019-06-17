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

import static io.ryos.rhino.sdk.utils.ReflectionUtils.getFieldByAnnotation;

import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.annotations.SessionFeeder;
import io.ryos.rhino.sdk.annotations.UserFeeder;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.users.data.User;
import java.util.Arrays;
import java.util.Objects;

/**
 * Injector implementation for default runner. The injector goes through all injection points, that
 * are class fields marked with {@link io.ryos.rhino.sdk.annotations.Feeder} annotation, and applies
 * injection strategy.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see DefaultSimulationRunner
 * @since 1.1.0
 */
public class DefaultRunnerSimulationInjector extends AbstractSimulationInjector {

  /**
   * Simulation metadata.
   * <p>
   */
  private final SimulationMetadata simulationMetadata;

  /**
   * Current user session, that is actively running.
   * <p>
   */
  private final UserSession userSession;

  /**
   * Instantiates a new {@link DefaultRunnerSimulationInjector} instance.
   * <p>
   *
   * @param simulationMetadata Simulation metadata provided by annotations.
   * @param userSession User session.
   */
  public DefaultRunnerSimulationInjector(final SimulationMetadata simulationMetadata,
      final UserSession userSession) {
    this.simulationMetadata = Objects.requireNonNull(simulationMetadata);
    this.userSession = userSession;
  }

  @Override
  public void injectOn(final Object injectable) {
    Objects.requireNonNull(injectable);

    var user = userSession.getUser();

    injectUser(user, injectable);// Each thread will run as the same user.
    injectSession(userSession, injectable);
    injectCustomFeeders(injectable);
  }

  private void injectSession(final UserSession userSession, final Object simulationInstance) {
    var fieldAnnotation = getFieldByAnnotation(simulationMetadata.getSimulationClass(),
        SessionFeeder.class);
    fieldAnnotation
        .ifPresent(f -> setValueToInjectionPoint(userSession, f.getFirst(), simulationInstance));
  }

  private void injectUser(final User user, final Object simulationInstance) {
    var fieldAnnotation = getFieldByAnnotation(simulationMetadata.getSimulationClass(),
        UserFeeder.class);
    fieldAnnotation
        .ifPresent(f -> setValueToInjectionPoint(user, f.getFirst(), simulationInstance));
  }

  /* Find the first annotation type, clazzAnnotation, on field declarations of the clazz.  */
  private void injectCustomFeeders(final Object simulationInstance) {

    Arrays.stream(simulationMetadata.getSimulationClass().getDeclaredFields())
        .filter(hasFeeder)
        .map(injectionPointFunction)
        .forEach(ip -> feed(simulationInstance, ip));
  }
}