/* ************************************************************************
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

package com.adobe.rhino.sdk.users;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClasspathUserProviderImpl implements UserProvider {
  private static final Logger LOG = LogManager.getLogger(ClasspathUserProviderImpl.class);

  private final String pathToFile;
  private final AtomicInteger counter = new AtomicInteger(0);

  public ClasspathUserProviderImpl(final String pathToCSVFile) {
    this.pathToFile = pathToCSVFile;
  }

  @Override
  public List<User> readUsers() {

    try (var isr = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(pathToFile)))) {

      final List<User> collect = isr.lines()
          .map(line -> line.split(";"))
          .filter(arr -> arr.length == 3)
          .map(arr -> new UserImpl(arr[0], arr[1], counter.incrementAndGet(), arr[2]))
          .collect(Collectors.toList());

      if (collect.isEmpty()) {
        LOG.info("No valid user found in " + pathToFile + ". The CSV file should contain lines in"
            + " the following format: username;password;scope");
      }

      return collect;

    } catch (IOException e) {
      LOG.error(e);
      return Collections.emptyList();
    }
  }
}
