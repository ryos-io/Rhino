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

import io.ryos.rhino.sdk.users.OAuthResponseData;
import io.ryos.rhino.sdk.users.oauth.OAuthService;

/**
 * Thrown if the authorization server's response cannot be mapped to
 * {@link OAuthResponseData} or {@link OAuthService} in case of service
 * authentication.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class InvalidAuthResponseException extends RuntimeException {

  /**
   * Constructs a new {@link InvalidAuthResponseException} instance.
   * <p>
   *
   * @param message Message of the exception.
   * @param cause Cause of the exception.
   */
  public InvalidAuthResponseException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
