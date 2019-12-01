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

public interface SessionDSL {

  /**
   * Session DSL is used to save objects to the current session.
   *
   * @param sessionKey     Session key.
   * @param objectFunction Object provider.
   * @return Runnable DSL instance.
   */
  RunnableDsl session(String sessionKey, Supplier<Object> objectFunction);
}
