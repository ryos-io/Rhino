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

package com.adobe.rhino.sdk;

import com.adobe.rhino.sdk.annotations.After;
import com.adobe.rhino.sdk.annotations.Before;
import com.adobe.rhino.sdk.annotations.Feeder;
import com.adobe.rhino.sdk.annotations.Logging;
import com.adobe.rhino.sdk.annotations.Scenario;
import com.adobe.rhino.sdk.annotations.SessionFeeder;
import com.adobe.rhino.sdk.annotations.Simulation;
import com.adobe.rhino.sdk.annotations.UserFeeder;
import com.adobe.rhino.sdk.data.UserSession;
import com.adobe.rhino.sdk.feeders.UUIDFeeder;
import com.adobe.rhino.sdk.reporting.GatlingLogFormatter;
import com.adobe.rhino.sdk.users.IMSUserRepositoryFactoryImpl;
import com.adobe.rhino.sdk.users.OAuthUserImpl;
import com.adobe.rhino.sdk.users.User;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

@Simulation(name = "Server-Status Simulation", durationInMins = 1)
@Logging(file = "/Users/bagdemir/sims/simulation.log", formatter = GatlingLogFormatter.class)
public class PerformanceTestingExample {

  @UserFeeder(max = 1, factory = IMSUserRepositoryFactoryImpl.class)
  private User user;

  @SessionFeeder
  private UserSession userSession;

  @Feeder(factory = UUIDFeeder.class)
  private String uuid;

  @Before
  public void setUp() {
  }

  @Scenario(name = "Health")
  public void performHealth(Recorder recorder) throws InterruptedException {
  }

  @After
  public void cleanUp() {
    // System.out.println("Clean up the test with user:" + user.getUsername());
  }
}
