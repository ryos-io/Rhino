/*
  Copyright 2018 Adobe.

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

package com.adobe.rhino.sdk.data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 */
public class InjectionPoint<T extends Annotation> {

  private final Field field;
  private final T annotation;

  public InjectionPoint(
      final Field field,
      final T annotation) {

    this.field = field;
    this.annotation = annotation;
  }

  public Field getField() {
    return field;
  }

  public T getAnnotation() {
    return annotation;
  }
}
