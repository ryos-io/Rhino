/**************************************************************************
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
 * Representation of a User, that is used in requests against the backend. Every request is
 * associated with an user.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @since 1.0.0
 */
public interface User {

  /**
   * User name.
   *
   * @return User name.
   */
  String getUsername();

  /**
   * Password, if authentication is required.
   *
   * @return Password.
   */
  String getPassword();

  /**
   * Scope of the user.
   *
   * @return Authorization scope.
   */
  String getScope();

  /**
   * A unique id to distinguish the user from others.
   *
   * @return The id of the user.
   */
  int getId();
}
