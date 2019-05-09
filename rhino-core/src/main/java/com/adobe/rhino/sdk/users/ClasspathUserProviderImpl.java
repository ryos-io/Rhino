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
