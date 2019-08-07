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

package io.ryos.rhino.sdk.users.source;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

import io.ryos.rhino.sdk.exceptions.ConfigurationNotFoundException;
import io.ryos.rhino.sdk.exceptions.NoUserFoundException;
import java.util.UUID;
import org.junit.Test;

public class FileBasedUserSourceTest {

  private static final String CSV_EMPTY_FILE = "classpath:///test_users_empty.csv";
  private static final String CSV_FILE = "classpath:///test_users.csv";
  private static final int EXPECTED_NUM_USERS = 9;

  @Test
  public void testGetUsers() {

    var fileBasedUserSource = new FileBasedUserSourceImpl(CSV_FILE);
    var users = fileBasedUserSource.getUsers();
    assertThat(users, notNullValue());
    assertThat(users.isEmpty(), equalTo(false));
    assertThat(users.size(), equalTo(EXPECTED_NUM_USERS));
  }

  @Test(expected = NoUserFoundException.class)
  public void testGetUsersFromEmptyFile() {
    var fileBasedUserSource = new FileBasedUserSourceImpl(CSV_EMPTY_FILE);
    fileBasedUserSource.getUsers();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetUsersWithInvalidPath() {
    var fileBasedUserSource = new FileBasedUserSourceImpl(UUID.randomUUID().toString());
    fileBasedUserSource.getUsers();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetUsersWithInvalidScheme() {
    var fileBasedUserSource =
        new FileBasedUserSourceImpl("invalid:///" + UUID.randomUUID().toString());
    fileBasedUserSource.getUsers();
  }

  @Test(expected = ConfigurationNotFoundException.class)
  public void testGetUsersWithNonexistingFile() {
    var fileBasedUserSource =
        new FileBasedUserSourceImpl("file:///" + UUID.randomUUID().toString());
    fileBasedUserSource.getUsers();
  }
}
