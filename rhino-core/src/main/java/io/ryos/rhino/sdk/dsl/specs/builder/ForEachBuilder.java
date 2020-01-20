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

package io.ryos.rhino.sdk.dsl.specs.builder;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.specs.ForEachDsl;
import io.ryos.rhino.sdk.dsl.specs.SessionDslItem.Scope;
import java.util.function.Function;

public interface ForEachBuilder<E, R extends Iterable<E>> {

  ForEachBuilder<E, R> doRun(Function<E, MaterializableDslItem> forEachFunction);

  ForEachBuilder<E, R> saveTo(String sessionKey);

  ForEachBuilder<E, R> saveTo(String sessionKey, Scope scope);

  Function<UserSession, R> getIterableSupplier();

  Function<E, ? extends MaterializableDslItem> getForEachFunction();

  String getKey();

  Scope getScope();

  ForEachDsl<E, R> getSpec();

  void setSpec(ForEachDsl<E, R> spec);
}
