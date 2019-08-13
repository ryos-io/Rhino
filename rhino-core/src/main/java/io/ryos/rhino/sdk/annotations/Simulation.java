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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@link Simulation} annotation is used to mark classes as Simulation entities. Simulation
 * entities contain the load test methods as well as metadata describing how to generate load
 * against a service instance under test.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Simulation {

  /**
   * The name of the simulation. Name must be unique if the package contains multiple simulations.
   * <p>
   *
   * @return name of the simulation.
   */
  String name() default "";

  /**
   * The region of the test users. The user source must provide as many users in that region that
   * the simulation requires, otherwise the simulation will not start.
   * <p>
   *
   * @return user region.
   */
  String userRegion() default "all";

  /**
   * The number of users that simulation employs. The user source must provide as many users that
   * the simulation requires, otherwise the simulation will not start.
   * <p>
   *
   * @return Number of users that Simulation requires.
   */
  int maxNumberOfUsers() default 2;

  /**
   * Duration of the Simulation in minutes.
   * <p>
   *
   * @return duration of the Simulation in minutes.
   */
  int durationInMins() default 1;
}
