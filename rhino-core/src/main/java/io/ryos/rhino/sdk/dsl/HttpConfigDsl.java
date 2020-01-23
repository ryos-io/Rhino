/*
 * Copyright 2020 Ryos.io.
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

package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.users.data.User;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Configurable Http spec.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface HttpConfigDsl extends HttpMethodDsl {

  HttpConfigDsl endpoint(String endpoint);

  HttpConfigDsl endpoint(Function<UserSession, String> endpoint);

  HttpConfigDsl endpoint(BiFunction<UserSession, HttpDsl, String> endpoint);

  /**
   * Adds a new header into headers.
   * <p>
   *
   * @param headerFunction Function to get the header value.
   * @return {@link HttpDsl} instance with headers initialized.
   */
  HttpConfigDsl header(Function<UserSession, Entry<String, List<String>>> headerFunction);

  HttpConfigDsl header(String key, List<String> values);

  HttpConfigDsl header(String key, String value);

  HttpConfigDsl formParam(Function<UserSession, Entry<String, List<String>>> formParamFunction);

  HttpConfigDsl formParam(String key, List<String> values);

  HttpConfigDsl formParam(String key, String value);

  HttpConfigDsl queryParam(Function<UserSession, Entry<String, List<String>>> queryParamFunction);

  HttpConfigDsl queryParam(String key, List<String> values);

  HttpConfigDsl queryParam(String key, String value);

  HttpConfigDsl auth();

  HttpConfigDsl auth(User user);

  HttpConfigDsl auth(Function<UserSession, User> sessionAccessor);

  /**
   * Requires an authorized user in HTTP requests.
   *
   * @param userSupplier User supplier.
   * @return Instance of {@link HttpConfigDsl}.
   */
  HttpConfigDsl auth(Supplier<User> userSupplier);

  HttpConfigDsl upload(final Supplier<InputStream> inputStream);

  Function<UserSession, User> getUserAccessor();

  Supplier<User> getUserSupplier();
}
