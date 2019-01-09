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

import com.adobe.rhino.sdk.data.UserSession;
import com.adobe.rhino.sdk.data.UserSessionImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class DefaultUserRepositoryImpl implements UserRepository<UserSession> {

  private Queue<User> users;

  public DefaultUserRepositoryImpl(UserProvider userProvider) {
    Objects.requireNonNull(userProvider);
    this.users = new LinkedBlockingQueue<>(userProvider.readUsers());
  }

  @Override
  public UserSession take() {
    User user = users.peek();
    users.add(user);
    return new UserSessionImpl(user);
  }

  @Override
  public boolean has(int numberOfUsers) {
    return users.size() >= numberOfUsers;
  }

  @Override
  public List<UserSession> getUserSessions() {
    return users.stream().map(UserSessionImpl::new).collect(Collectors.toList());
  }
}
