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

package io.ryos.rhino.sdk.users;

import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.data.UserImpl;
import io.ryos.rhino.sdk.users.source.FileBasedUserSourceImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CSVUserParserImpl implements UserParser {

  private static final Logger LOG = LogManager.getLogger(CSVUserParserImpl.class);

  @Override
  public List<User> unmarshal(InputStream inputStream) {
    if (inputStream == null) {
      throw new RuntimeException("User file not found.");
    }
    var userList = new ArrayList<User>();
    try (var isr = new BufferedReader(new InputStreamReader(inputStream))) {
      return isr.lines()
          .map(line -> line.split(";"))
          .filter(arr -> arr.length == 4)
          .map(arr -> new UserImpl(arr[0], arr[1], "user-" + UUID.randomUUID(), arr[2], arr[3]))
          .collect(Collectors.toList());
    } catch (IOException e) {
      LOG.error(e); // TODO
    }
    return userList;
  }
}
