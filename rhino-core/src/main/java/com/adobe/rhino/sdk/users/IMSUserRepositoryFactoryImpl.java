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

import com.adobe.rhino.sdk.SimulationConfig;
import com.adobe.rhino.sdk.data.UserSession;

/**
 * Factory class for IMS user repository which creates a new {@link UserRepository} provides
 * {@link User} instances authenticated.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 */
public class IMSUserRepositoryFactoryImpl implements UserRepositoryFactory<UserSession> {

  private final String pathToUsers;

  public IMSUserRepositoryFactoryImpl(final String pathToUsers) {
    this.pathToUsers = pathToUsers;
  }

  public IMSUserRepositoryFactoryImpl() {
    final String userSource = SimulationConfig.getUserSource();
    final String absolutePath = userSource.replace("classpath://", "");
    this.pathToUsers = absolutePath;
  }

  @Override
  public UserRepository<UserSession> create() {
    return new IMSUserRepositoryImpl(new ClasspathUserProviderImpl(pathToUsers)).authenticateAll();
  }
}
