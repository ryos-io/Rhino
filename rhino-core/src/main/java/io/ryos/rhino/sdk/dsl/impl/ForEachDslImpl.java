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

package io.ryos.rhino.sdk.dsl.impl;

import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.getActiveUser;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.DslItem;
import io.ryos.rhino.sdk.dsl.ForEachDsl;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.ResultingDsl;
import io.ryos.rhino.sdk.dsl.SessionDslItem;
import io.ryos.rhino.sdk.dsl.mat.CollectingMaterializer;
import io.ryos.rhino.sdk.dsl.mat.DslMaterializer;
import io.ryos.rhino.sdk.dsl.mat.ForEachDslMaterializer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * For-each loop Dsl.
 *
 * @author Erhan Bagdemir
 */
public class ForEachDslImpl<S, R extends Iterable<S>, T extends MaterializableDslItem> extends
    AbstractSessionDslItem implements
    ForEachDsl {

  /**
   * Child DSL items.
   */
  private List<MaterializableDslItem> children;

  /**
   * Function is a provider of iterable object instances.
   */
  private Function<UserSession, R> iterableSupplier;

  /**
   * For each function.
   */
  private List<Function<S, T>> forEachFunctions;

  private Function<S, Object> mapper;

  /**
   * Constructs a new {@link ForEachDsl} instance.
   *
   * @param name Spec name.
   * @param children Child DSL items.
   * @param sessionKey Session key.
   * @param scope Session scope.
   * @param iterableSupplier Supplier for iterable.
   */
  public ForEachDslImpl(final String name,
      final List<MaterializableDslItem> children,
      final String sessionKey,
      final Scope scope,
      final Function<UserSession, R> iterableSupplier,
      final List<Function<S, T>> forEachFunctions,
      final Function<S, Object> mapper) {

    super(name, sessionKey, scope);

    this.children = children;
    this.iterableSupplier = iterableSupplier;
    this.forEachFunctions = forEachFunctions;
    this.mapper = mapper;
  }

  @Override
  public DslMaterializer materializer() {
    return mapper != null ? new CollectingMaterializer<>(this) : new ForEachDslMaterializer<>(this);
  }

  @Override
  public List<MaterializableDslItem> getChildren() {
    return children;
  }

  @Override
  public Function<UserSession, R> getIterableSupplier() {
    return iterableSupplier;
  }

  @Override
  public List<Function<S, T>> getForEachFunctions() {
    return forEachFunctions;
  }

  @Override
  public Function<S, Object> getMapper() {
    return mapper;
  }

  @Override
  public UserSession handleResult(final UserSession userSession, final Object response) {
    final SessionDslItem sessionDslItem = this;

    List<Object> listOfObjects = new CopyOnWriteArrayList<>();
    final ResultingDsl resultingDsl = resolveResultableParent();

    if (!hasParent() || resultingDsl == null) {
      if (sessionDslItem.getSessionScope().equals(Scope.USER)) {
        listOfObjects = userSession.<List<Object>>get(sessionDslItem.getSessionKey())
            .orElse(new CopyOnWriteArrayList<>());
        listOfObjects.add(response);
        userSession.add(sessionDslItem.getSessionKey(), listOfObjects);
      } else {
        var activatedUser = getActiveUser(userSession);
        var globalSession = userSession.getSimulationSessionFor(activatedUser);
        listOfObjects = globalSession.<List<Object>>get(sessionDslItem.getSessionKey())
            .orElse(new CopyOnWriteArrayList<>());
        listOfObjects.add(response);
        globalSession.add(sessionDslItem.getSessionKey(), listOfObjects);
      }
      return userSession;
    }

    return delegateToParent(userSession, listOfObjects, resultingDsl);
  }

  private UserSession delegateToParent(UserSession userSession, List<Object> listOfObjects,
      ResultingDsl resultingDsl) {
    return resultingDsl.handleResult(userSession, listOfObjects);
  }

  private ResultingDsl resolveResultableParent() {
    DslItem current = getParent();
    ResultingDsl resultingDsl = null;
    while (current != null) {
      if (current instanceof ResultingDsl) {
        resultingDsl = (ResultingDsl) current;
      }
      current = current.getParent();
    }
    return resultingDsl;
  }

  @Override
  public String getSaveTo() {
    return null;
  }
}
