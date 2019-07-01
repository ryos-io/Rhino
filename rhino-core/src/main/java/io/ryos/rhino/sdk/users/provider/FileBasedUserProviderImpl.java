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

package io.ryos.rhino.sdk.users.provider;

import io.ryos.rhino.sdk.io.ConfigResource;
import io.ryos.rhino.sdk.users.CSVUserParserImpl;
import io.ryos.rhino.sdk.users.UserParser;
import io.ryos.rhino.sdk.users.data.User;
import java.util.List;

/**
 * File based implementation of {@link UserProvider}.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class FileBasedUserProviderImpl implements UserProvider {
  private final String pathToFile;
  private final UserParser parser = new CSVUserParserImpl();

  public FileBasedUserProviderImpl(final String pathToCSVFile) {
    this.pathToFile = pathToCSVFile;
  }

  @Override
  public List<User> getUsers() {
    var userList = parser.unmarshal(new ConfigResource(pathToFile).getInputStream());
    if (userList.isEmpty()) {
      throw new RuntimeException("No valid user found in " + pathToFile + ". The CSV file should contain "
          + "lines in the following format: username;password;scope");
    }
    return userList;
  }
}
