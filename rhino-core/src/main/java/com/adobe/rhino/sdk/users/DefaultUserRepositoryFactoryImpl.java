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

import static java.util.stream.Collectors.toList;

import com.adobe.rhino.sdk.data.UserSession;
import com.adobe.rhino.sdk.data.UserSessionImpl;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * User repository, if nothing else provided.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 */
public class DefaultUserRepositoryFactoryImpl implements UserRepositoryFactory<UserSession> {

  private static final int START = 1;
  private static final int END = 10;

  public DefaultUserRepositoryFactoryImpl(final long loginDelay) {
    // no delay needed in
  }

  @Override
  public UserRepository<UserSession> create() {

    return new DefaultUserRepositoryImpl(() -> IntStream
        .rangeClosed(START, END)
        .mapToObj(id -> new UserImpl("User-" + UUID.randomUUID(), null,
            id, null)).collect(toList()));
  }
}
