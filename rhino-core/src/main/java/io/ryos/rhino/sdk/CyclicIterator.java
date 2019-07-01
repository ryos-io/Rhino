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

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Cyclic iterator iterates over the list provided, and once the list is over it starts from the
 * beginning till the iteration is stopped by calling {@link #stop()} to do so, explicitly. The
 * iterator is used as generator for the load generator system.
 * <p>
 *
 * @author Erhan Bagdemir
 * @version 1.0.0
 */
@ThreadSafe
public class CyclicIterator<T> implements Iterator<T> {

  /**
   * Cursor point to the current item. The cursor will be incremented by 1 every time the next
   * method is called.
   * <p>
   */
  private final AtomicInteger cursor = new AtomicInteger(-1);

  /**
   * The list to be iterated over.
   * <p>
   */
  private final List<T> list;

  /**
   * Whether the iterator has more items.
   * <p>
   */
  private volatile boolean hasNext = true;

  /**
   * Creates a new {@link CyclicIterator} instance.
   * <p>
   *
   * @param list The list backing the iterator.
   */
  public CyclicIterator(final List<T> list) {

    if (list.isEmpty()) { throw new IllegalArgumentException("Backing list in CyclicIterator is "
        + "empty."); }

    this.list = list;
  }

  /**
   * The method returns a boolean value indicating whether the iterator has more items.
   * <p>
   *
   * @return boolean, whether the iterator has more items.
   */
  @Override
  public boolean hasNext() {
    return hasNext;
  }

  /**
   * Returns the next item where the cursor points to. Calling this method sets the cursor forward
   * to point the next item in the list. If the cursor is at the end of the list, it begins from
   * the first item.
   * <p>
   *
   * @return The current item, pointed by the cursor.
   */
  @Override
  public T next() {

    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    cursor.getAndUpdate(p -> (++p) % list.size());

    return list.get(cursor.get());
  }

  /**
   * Sets the hasNext attribute to false. Every next method call after stop causes a
   * {@link NoSuchElementException}.
   * <p>
   */
  public void stop() {
    this.hasNext = false;
  }
}
