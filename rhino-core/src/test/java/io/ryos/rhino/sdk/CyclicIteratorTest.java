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

package io.ryos.rhino.sdk;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import org.junit.Test;

public class CyclicIteratorTest {

  @Test
  public void testEmptyIterator() {
    CyclicIterator<Integer> it = new CyclicIterator<>(Collections.emptyList());
    assertThat(it.hasNext(), equalTo(false));
  }

  @Test
  public void testSingleItemIterator() {
    ArrayList<Integer> numbers = new ArrayList<>();
    numbers.add(1);

    CyclicIterator<Integer> it = new CyclicIterator<>(numbers);
    assertThat(it.hasNext(), equalTo(true));
    assertThat(it.next(), equalTo(1));
    assertThat(it.next(), equalTo(1));
  }

  @Test
  public void testMultipleItemIterator() {
    ArrayList<Integer> numbers = new ArrayList<>();
    numbers.add(1);
    numbers.add(2);

    CyclicIterator<Integer> it = new CyclicIterator<>(numbers);
    assertThat(it.hasNext(), equalTo(true));
    assertThat(it.next(), equalTo(1));
    assertThat(it.hasNext(), equalTo(true));
    assertThat(it.next(), equalTo(2));
    assertThat(it.hasNext(), equalTo(true));
    assertThat(it.next(), equalTo(1));
  }
}
