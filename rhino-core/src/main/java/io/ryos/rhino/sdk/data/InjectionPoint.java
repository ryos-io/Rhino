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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Injection point holds the information where to inject an instance.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class InjectionPoint<T extends Annotation> {

  /**
   * Field where the injection happens.
   * <p>
   */
  private final Field field;

  /**
   * Annotation which marks the injection point.
   * <p>
   */
  private final T annotation;

  /**
   * Constructs a new {@link InjectionPoint} instance.
   * <p>
   *
   * @param field Field injection point.
   * @param annotation The annotation which the injection point is marked.
   */
  public InjectionPoint(Field field, T annotation) {

    this.field = field;
    this.annotation = annotation;
  }

  /**
   * Getter for the field instance where the injection happens.
   * <p>
   *
   * @return The field instance where the injection happens.
   */
  public Field getField() {
    return field;
  }

  /**
   * Getter for the annotation which marks the injection point.
   * <p>
   *
   * @return The annotation which marks the injection point.
   */
  public T getAnnotation() {
    return annotation;
  }
}
