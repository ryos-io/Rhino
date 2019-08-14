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

package io.ryos.rhino.sdk.users.data;

import java.util.Objects;

public class UserImpl implements User {

  private final String username;
  private final String password;
  private final String id;
  private final String scope;
  private final String region;

  public UserImpl(String username, String password, String id, String scope, String region) {
    this.username = username;
    this.password = password;
    this.id = id;
    this.scope = scope;
    this.region = region;
  }

  public UserImpl(String username, String password, String id, String scope) {
    this(username, password, id, scope, "default");
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
  public String getId() {
    return id;
  }

  @Override
  public String getRegion() {
    return region;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final UserImpl user = (UserImpl) o;
    return username.equals(user.username) &&
        id.equals(user.id) &&
        region.equals(user.region);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, id, region);
  }
}
