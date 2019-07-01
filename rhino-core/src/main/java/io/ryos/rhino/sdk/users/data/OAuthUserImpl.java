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

/**
 * Implementation of authenticated user representation.
 *
 * @author Erhan Bagdemir
 * @since 1.0.0
 */
public class OAuthUserImpl extends UserImpl implements OAuthUser {

  private String accessToken;
  private String refreshToken;
  private String scope;
  private String clientId;

  public OAuthUserImpl(final String user,
      final String password,
      final String accessToken,
      final String refreshToken,
      final String scope,
      final String clientId,
      final String id) {

    super(user, password, id, scope);

    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.clientId = clientId;
    this.scope = scope;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  @Override
  public String getAccessToken() {
    return accessToken;
  }

  @Override
  public String getRefreshToken() {
    return refreshToken;
  }

  @Override
  public String getScope() {
    return scope;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  @Override
  public String toString() {
    return "OAuthUserImpl{" +
        "userName='" + getUsername() + '\'' +
        "clientId='" + getClientId() + '\'' +
        '}';
  }
}
