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
import io.ryos.rhino.sdk.dsl.impl.LoadDslImpl;
import java.util.function.Predicate;

public interface AssertionDsl extends DslItem {

  /**
   * Ensure DSL is to assert the predicate passed holds true, otherwise it stops the pipeline.
   *
   * @return {@link LoadDslImpl} instance.
   */
  LoadDsl ensure(Predicate<UserSession> predicate);

  /**
   * Ensure DSL is to assert the predicate passed holds true, otherwise it stops the pipeline.
   *
   * @return {@link LoadDslImpl} instance.
   */
  LoadDsl ensure(Predicate<UserSession> predicate, String reason);
}
