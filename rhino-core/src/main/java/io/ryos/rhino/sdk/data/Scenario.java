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

package io.ryos.rhino.sdk.data;

import java.lang.reflect.Method;

/**
 * Scenario representation.
 *
 * @author Erhan Bagdemir
 * @since 1.0.0
 */
public class Scenario {

  /**
   * Method to be executed.
   */
  private final Method method;

  /**
   * Description of the scenario used in reports.
   */
  private final String description;

  public Scenario(final String description, final Method method) {
    this.method = method;
    this.description = description;
  }

  /**
   * @return The method instance.
   */
  public Method getMethod() {
    return method;
  }

  /**
   * @return The description.
   */
  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return "Scenario{" +
        "description='" + description + '\'' +
        '}';
  }
}