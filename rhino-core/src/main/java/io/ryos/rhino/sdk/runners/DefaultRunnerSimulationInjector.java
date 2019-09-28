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

import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.data.Pair;
import io.ryos.rhino.sdk.providers.OAuthUserProvider;
import io.ryos.rhino.sdk.users.repositories.CyclicUserSessionRepositoryImpl;

import java.util.Objects;

import static io.ryos.rhino.sdk.utils.ReflectionUtils.getFieldsByAnnotation;

/**
 * Injector implementation for default runner. The injector goes through all injection points, that
 * are class fields marked with {@link Provider} annotation, and applies
 * injection strategy.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see DefaultSimulationRunner
 * @since 1.1.0
 */
public class DefaultRunnerSimulationInjector extends AbstractSimulationInjector {

  /**
   * Instantiates a new {@link DefaultRunnerSimulationInjector} instance.
   * <p>
   *
   * @param simulationMetadata Simulation metadata provided by annotations.
   */
  public DefaultRunnerSimulationInjector(SimulationMetadata simulationMetadata) {
    super(simulationMetadata);
  }

  @Override
  public void injectOn(final Object injectable) {
    Objects.requireNonNull(injectable);
    injectUser(injectable);// Each thread will run as the same user.
    injectCustomFeeders(injectable);
  }

  private void injectUser(final Object simulationInstance) {
    var fieldAnnotation = getFieldsByAnnotation(getSimulationMetadata().getSimulationClass(), UserProvider.class);

    fieldAnnotation.stream().map(pair ->
        new Pair<>(new OAuthUserProvider(new CyclicUserSessionRepositoryImpl(getSimulationMetadata().getUserRepository(),
            pair.getSecond().region())), pair.getFirst()))
        .forEach(r -> setValueToInjectionPoint(r.getFirst(), r.getSecond(), simulationInstance));
  }
}
