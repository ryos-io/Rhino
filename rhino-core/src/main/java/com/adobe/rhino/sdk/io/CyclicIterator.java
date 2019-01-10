/**************************************************************************
 *                                                                        *
 * ADOBE CONFIDENTIAL                                                     *
 * ___________________                                                    *
 *                                                                        *
 *  Copyright 2018 Adobe Systems Incorporated                             *
 *  All Rights Reserved.                                                  *
 *                                                                        *
 * NOTICE:  All information contained herein is, and remains              *
 * the property of Adobe Systems Incorporated and its suppliers,          *
 * if any.  The intellectual and technical concepts contained             *
 * herein are proprietary to Adobe Systems Incorporated and its           *
 * suppliers and are protected by trade secret or copyright law.          *
 * Dissemination of this information or reproduction of this material     *
 * is strictly forbidden unless prior written permission is obtained      *
 * from Adobe Systems Incorporated.                                       *
 **************************************************************************/

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
