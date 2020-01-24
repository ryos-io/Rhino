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

package io.ryos.rhino.sdk.dsl;

import java.util.function.Supplier;

/**
 * Session DSL is used to store object in the session.
 *
 * @author Erhan Bagdemir
 */
public interface SessionDsl extends DslItem {

  /**
   * Session DSL is used to save objects to the current session which are provided by the object
   * supplier.
   *
   * @param sessionKey     Session key.
   * @param objectSupplier Object provider.
   * @return Runnable DSL instance.
   */
  LoadDsl session(String sessionKey, Supplier<Object> objectSupplier);

  /**
   * Session DSL is used to save objects to the current session.
   *
   * @param object Object instance to store in the session.
   * @return {@link LoadDsl} instance.
   */
  LoadDsl session(String sessionKey, Object object);
}
