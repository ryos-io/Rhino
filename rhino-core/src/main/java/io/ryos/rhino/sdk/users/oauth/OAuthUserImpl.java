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

package io.ryos.rhino.sdk.users.oauth;

import io.ryos.rhino.sdk.users.data.UserImpl;

/**
 * Implementation of authenticated user representation.
 *
 * @author Erhan Bagdemir
 * @since 1.0.0
 */
public class OAuthUserImpl extends UserImpl implements OAuthUser {

  private OAuthService service;
  private String accessToken;
  private String refreshToken;
  private String clientId;

  public OAuthUserImpl(final OAuthService service,
      final String user,
      final String password,
      final String accessToken,
      final String refreshToken,
      final String scope,
      final String clientId,
      final String id,
      final String region) {

    super(user, password, id, scope, region);

    this.service = service;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.clientId = clientId;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
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
  public String getClientId() {
    return clientId;
  }

  @Override
  public OAuthService getOAuthService() {
    return service;
  }

  @Override
  public String toString() {
    return "OAuthUserImpl{" +
        "userName='" + getUsername() + '\'' +
        "clientId='" + getClientId() + '\'' +
        '}';
  }
}
