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

package io.ryos.rhino.sdk.dsl.mat;

import static io.ryos.rhino.sdk.dsl.utils.DslUtils.eval;
import static io.ryos.rhino.sdk.dsl.utils.DslUtils.forEach;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import io.ryos.rhino.sdk.data.UserSessionImpl;
import io.ryos.rhino.sdk.dsl.impl.DslBuilderImpl;
import io.ryos.rhino.sdk.users.data.UserImpl;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.Test;

public class ForEachDslMaterializerTest {

  @Test
  public void testForEach() {
    DslBuilderImpl.dslMethodName.set("test");
    var user = new UserSessionImpl(new UserImpl("user", UUID.randomUUID().toString(), "", ""));
    var collectedNumbers = new ArrayList<Integer>();
    var dslBuilder = forEach(ImmutableList.of(1, 2),
        i -> eval(s -> collectedNumbers.add(i)));

    dslBuilder.materializer().materialize(user).block();
    assertEquals(2, collectedNumbers.size());
    assertEquals(1, (int) collectedNumbers.get(0));
    assertEquals(2, (int) collectedNumbers.get(1));
  }
}
