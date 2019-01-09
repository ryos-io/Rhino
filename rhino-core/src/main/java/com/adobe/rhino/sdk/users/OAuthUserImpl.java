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
      final int id) {

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
