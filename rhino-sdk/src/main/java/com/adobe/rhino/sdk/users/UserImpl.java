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

/**
 *
 */
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
