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

package io.ryos.rhino.sdk.exceptions;

import java.lang.reflect.Constructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Exception utils.
 *
 * @author <a href="mailto:erhan@ryos.io">Erhan Bagdemir</a>
 * @since 1.0
 */
public class Exceptions {

  private static final Logger LOG = LogManager.getLogger(Exceptions.class);

  /**
   * Rethrow any instance of {@link RuntimeException} by wrapping it into a new one, of which type
   * passed as method parameter while logging the error out.
   *
   * @param e Exception instance, that is caught.
   * @param exception The new exception to be rethrown.
   * @param logMsg Log message.
   */
  public static void rethrow(Exception e, Class<? extends RuntimeException> exception,
      String logMsg) {
    if (logMsg != null) {
      LOG.error(logMsg, e);
    }
    final Constructor<? extends Exception> declaredConstructor;
    try {
      declaredConstructor = exception.getDeclaredConstructor(Throwable.class);
      throw declaredConstructor.newInstance(e);
    } catch (Exception e1) {
      LOG.error(e1);
    }
  }

  /**
   * Rethrow any instance of {@link RuntimeException} by wrapping it into a new one, of which type
   * passed as method parameter.
   *
   * @param e Exception instance, that is caught.
   * @param exception The new exception to be rethrown.
   */
  public static void rethrow(Exception e, Class<? extends RuntimeException> exception) {
    rethrow(e, exception, null);
  }
}
