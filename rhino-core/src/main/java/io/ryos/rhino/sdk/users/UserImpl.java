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

package io.ryos.rhino.sdk.users;

public class UserImpl implements User {

  private final String username;
  private final String password;
  private final int id;
  private final String scope;

  public UserImpl(final String username, final String password, final int id,
      final String scope) {
    this.username = username;
    this.password = password;
    this.id = id;
    this.scope = scope;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getScope() {
    return scope;
  }

  @Override
  public int getId() {
    return id;
  }
}
