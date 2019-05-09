/*
  Copyright 2018 Adobe.

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

package com.adobe.rhino.sdk.io;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cyclic iterator iterates over the list provided, and once the list is over it starts from the
 * beginning till it is stopped by calling {@link #stop()} to do so, explicitly. The iterator is
 * used as generator for the load generator system.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 */
public class CyclicIterator<T> implements Iterator<T> {

  // Cursor point to the current item.
  private final AtomicInteger cursor = new AtomicInteger(-1);

  // The list to be iterated over.
  private final List<T> list;

  private volatile boolean hasNext = true;

  public CyclicIterator(final List<T> list) {
    this.list = list;
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  @Override
  public T next() {

    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    cursor.getAndUpdate(p -> (p + 1) % list.size());

    return list.get(cursor.get());
  }

  public void stop() {
    this.hasNext = false;
  }
}
