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

import io.ryos.rhino.sdk.dsl.SessionDslItem.Scope;
import java.util.function.Supplier;

/**
 * Session DSL is used to store object in the define.
 *
 * @author Erhan Bagdemir
 */
public interface SessionDsl extends DslItem {

  /**
   * Session DSL is used to save objects to the current define which are provided by the object
   * supplier. Use {@link #define(String, Supplier)} instead.
   *
   * @param sessionKey     Session key.
   * @param objectSupplier Object provider.
   * @return Runnable DSL instance.
   */
  @Deprecated
  DslBuilder session(String sessionKey, Supplier<Object> objectSupplier);

  /**
   * Session DSL is used to save objects to the current define.  Use {@link #define(String, Object)} instead.
   *
   * @param object Object instance to store in the define.
   * @return {@link DslBuilder} instance.
   */
  @Deprecated
  DslBuilder session(String sessionKey, Object object);

  DslBuilder define(String sessionKey, Supplier<Object> objectSupplier);
  DslBuilder define(String sessionKey, Object object);
  DslBuilder define(String sessionKey, Object object, Scope scope);
  DslBuilder define(String sessionKey, Supplier<Object> objectSupplier, Scope scope);

}
