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

package io.ryos.rhino.sdk.dsl.data.builder;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.ForEachDsl;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.SessionDslItem.Scope;
import java.util.List;
import java.util.function.Function;

public interface ForEachBuilder<E, R extends Iterable<E>, T extends MaterializableDslItem> {

  ForEachBuilder<E, R, T> exec(Function<E, T> forEachFunction);

  ForEachMapBuilder<E, R, T> map(Function<E, Object> mapper);

  Function<UserSession, R> getIterableSupplier();

  Function<E, T> getForEachChildDslItemFunction();

  List<Function<E, T>> getForEachChildDslItemFunctions();

  Function<E, Object> getMapper();

  String getSessionKey();

  Scope getSessionScope();

  ForEachDsl<E, R> getSpec();

  void setSpec(ForEachDsl<E, R> spec);
}
