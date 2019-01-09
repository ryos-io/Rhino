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

package com.adobe.rhino.sdk.data;

import com.adobe.rhino.sdk.users.User;

/**
 * User session is a stash to store objects and share them among scenarios per user session. A
 * user will be created before the simulation starts, and it will existing during the simulation
 * execution.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @since 1.1.0
 */
public class UserSessionImpl extends ContextImpl implements UserSession {

  private User user;

  public UserSessionImpl(final User user) {
    this.user = user;
  }

  @Override
  public User getUser() {
    return user;
  }
}
