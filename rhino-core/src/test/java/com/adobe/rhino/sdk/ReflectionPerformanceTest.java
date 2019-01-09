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

package com.adobe.rhino.sdk;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;

/**
 * TODO Fix the doc. Created on 18.12.18.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
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
