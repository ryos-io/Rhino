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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.ryos.rhino.sdk.utils.ReflectionUtils.getFieldsByAnnotation;

/**
 * Injector for reactive runner. The difference from {@link DefaultRunnerSimulationInjector} is the
 * reactive variant does inject the repository classes which return values to the DSL instances,
 * whereas the default injector implementation does inject the value itself into the injection
 * points, that are marked with {@link Provider} annotation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see ReactiveHttpSimulationRunner
 * @see DefaultRunnerSimulationInjector
 * @since 1.1.0
 */
public class ReactiveRunnerSimulationInjector extends AbstractSimulationInjector {

  private static final Logger LOG = LogManager.getLogger(ReactiveRunnerSimulationInjector.class);

  /**
   * Instantiates a new {@link ReactiveRunnerSimulationInjector} instance.
   * <p>
   *
   * @param simulationMetadata Simulation metadata.
   */
  public ReactiveRunnerSimulationInjector(final SimulationMetadata simulationMetadata) {
    super(simulationMetadata);
  }

  @Override
  public void injectOn(Object injectable) {
    injectUserProvider(injectable); // Each thread will run as the same user.
    injectCustomFeeders(injectable);
  }

  private void injectUserProvider(final Object simulationInstance) {

    var fieldAnnotation = getFieldsByAnnotation(getSimulationMetadata().getSimulationClass(), UserProvider.class);

    fieldAnnotation.stream().map(pair ->
        new Pair<>(new OAuthUserProvider(new CyclicUserSessionRepositoryImpl(getSimulationMetadata().getUserRepository(),
            pair.getSecond().region())), pair.getFirst()))
        .forEach(r -> setValueToInjectionPoint(r.getFirst(), r.getSecond(), simulationInstance));
  }
}
