/*
  Copyright 2018 Ryos.io.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package io.ryos.rhino.sdk;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;

/**
 * @author Erhan Bagdemir
 */
public class ReflectionPerformanceTest {

  @Test
  public void testReflectionPerformance()
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

    final long l = System.currentTimeMillis();
    final PerformanceTestingExample performanceTestingExample = new PerformanceTestingExample();

    System.out.println(System.currentTimeMillis() - l);

    final long l2 = System.currentTimeMillis();
    final Constructor<PerformanceTestingExample> declaredConstructor = PerformanceTestingExample.class
        .getDeclaredConstructor();
    final PerformanceTestingExample performanceTestingExample1 = declaredConstructor.newInstance();

    System.out.println(System.currentTimeMillis() - l2);
  }
}
