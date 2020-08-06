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

package io.ryos.rhino.sdk.simulations;

import static io.ryos.rhino.sdk.dsl.DslBuilder.dsl;
import static io.ryos.rhino.sdk.dsl.data.builder.ForEachBuilderImpl.in;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.forEach;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.runIf;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.define;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.some;
import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.session;

import com.google.common.collect.ImmutableList;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.DslBuilder;
import io.ryos.rhino.sdk.dsl.utils.SessionUtils;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;

@Simulation(name = "Reactive Multi-User Test")
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
public class ForEachTestSimulation {

  @Dsl(name="test")
  public DslBuilder setUp() {
    return dsl()
        .session("index", () -> ImmutableList.of(1, 2, 3))
        .forEach(in(session("index")).map(i -> (int)i *2).collect("doubles"))
        .forEach(ImmutableList.of(1, 2, 3), i1 ->
             forEach(ImmutableList.of(1, 2, 3), i2 ->
                 runIf(session -> i1 * i2 % 2 == 0, define("calc",() -> i1 * i2))))
        .run(some("output").exec(s -> {
          s.get("calc").ifPresent(System.out::println);
          System.out.println(s.getSimulationSession().get("total"));
          return "OK";
        }));
  }
}
