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

package io.ryos.rhino.sdk.data;

import io.ryos.rhino.sdk.users.data.User;

/**
 * Simulation define is a context instance of which life cycle is spanning from the beginning of
 * simulations till they complete. In contrary to {@link UserSession} which life cycle ends after
 * every test execution, the simulation define retain its state until the end of the simulation
 * which makes the simulation sessions handy for simulation preparation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class SimulationSession extends ContextImpl {

  private final User user;

  public SimulationSession(User user) {
    this.user = user;
  }

  public User getUser() {
    return user;
  }
}
