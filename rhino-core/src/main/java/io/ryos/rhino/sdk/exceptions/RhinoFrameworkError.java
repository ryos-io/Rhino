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
 * Thrown if an unexpected framework error occurs. This error must be reported on Github issues
 * page.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class RhinoFrameworkError extends Error {

  public RhinoFrameworkError(final String message, final Throwable cause) {
    super(message, cause);
  }

  public RhinoFrameworkError(final String message) {
    super(message);
  }

  public RhinoFrameworkError() {
    super("Unexpected framework exception. "
        + "Please report this issue on https://github.com/ryos-io/Rhino/issues with the exception stack.");
  }
}
