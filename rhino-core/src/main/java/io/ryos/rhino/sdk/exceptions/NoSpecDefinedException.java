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

package io.ryos.rhino.sdk.exceptions;

/**
 * The exception is thrown if there is no spec defined in {@link io.ryos.rhino.sdk.dsl.LoadDsl}. The
 * pipeline will then be terminated.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.6.0
 */
public class NoSpecDefinedException extends RuntimeException {

  public NoSpecDefinedException(String message) {
    super(message);
  }
}
