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

import io.ryos.rhino.sdk.users.UserParser;
import io.ryos.rhino.sdk.users.VaultUserParserImpl;
import io.ryos.rhino.sdk.users.data.User;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Classpath file implementation of {@link UserProvider}.
 *
 * @author Erhan Bagdemir
 */
public class ClasspathUserProviderImpl implements UserProvider {

  private static final Logger LOG = LogManager.getLogger(ClasspathUserProviderImpl.class);

  private final String pathToFile;
  private final UserParser parser = new VaultUserParserImpl();

  public ClasspathUserProviderImpl(final String pathToCSVFile) {
    this.pathToFile = pathToCSVFile;
  }

  @Override
  public List<User> getUsers() {
    List<User> userList = parser.unmarshall(getClass().getResourceAsStream(pathToFile));
    if (userList.isEmpty()) {
      LOG.info("No valid user found in " + pathToFile + ". The CSV file should contain lines in"
          + " the following format: username;password;scope");
    }
    return userList;
  }
}
