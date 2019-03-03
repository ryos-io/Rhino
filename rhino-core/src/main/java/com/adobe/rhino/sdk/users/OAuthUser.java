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
 * Represents an authenticated user.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @since 1.0.0
 */
public interface OAuthUser extends User {

  /**
   * Access token.
   *
   * @return Access token.
   */
  String getAccessToken();

  /**
   * Refresh token.
   *
   * @return Refresh token.
   */
  String getRefreshToken();

  /**
   * Authorized scope.
   *
   * @return Authorized scope.
   */
  String getScope();

  /**
   * Client id of the user.
   *
   * @return Client id.
   */
  String getClientId();
}
