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

import io.ryos.rhino.sdk.data.UserSession;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation is used to mark static method which are run global the simulation is started
 * for each user. It allows test developers to prepare the simulation global the simulation run
 * e.g create some resources on server.
 * <p>
 *
 * The prepare method is useful to allocate resources used by scenarios run in simulations. The
 * test developers might create some resources in prepare method so that scenarios can access the
 * same resources without having need of creating them, repeatedly.
 * <p>
 *
 * Prepare method must be public and static:
 *
 * <code>
 * @Prepare
 * public static void prepare(SimulationSession in) {
 * // your code here.
 * }
 * </code>
 *
 * Prepare method might take an argument which is {@link io.ryos.rhino.sdk.data.SimulationSession}
 * a global context, that might be accessed by scenarios. The simulation in will not be
 * cleaned up after every scenario execution, but in clean-up method after the simulation completes.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see CleanUp
 * @see io.ryos.rhino.sdk.data.SimulationSession
 * @see UserSession
 * @since 1.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Prepare {

}
