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

package io.ryos.rhino.sdk.annotations;

import io.ryos.rhino.sdk.runners.DefaultSimulationRunner;
import io.ryos.rhino.sdk.runners.SimulationRunner;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare which implementation of {@link SimulationRunner} will be used. Default
 * runner, that is {@link DefaultSimulationRunner}, is implemented in push-style, so that the
 * generated load will cause the scenarios to get called. If the scenario implementation is
 * blocking, then the caller thread will get blocked, as well.
 * <p>
 *
 * The number of threads which will be employed, can be configured in rhino.properties file with
 * the property, "runner.parallelisation".
 * <p>
 *
 * @author Erhan Bagdemir
 * @see SimulationRunner
 * @since 1.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Runner {

  Class<? extends SimulationRunner> clazz() default DefaultSimulationRunner.class;
}
