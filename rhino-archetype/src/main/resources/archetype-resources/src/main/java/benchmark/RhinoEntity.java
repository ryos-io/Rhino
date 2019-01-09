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

package ${groupId}.benchmark;

import com.adobe.rhino.sdk.users.User;

import com.adobe.rhino.sdk.Recorder;
import com.adobe.rhino.sdk.annotations.CleanUp;
import com.adobe.rhino.sdk.annotations.Feeder;
import com.adobe.rhino.sdk.annotations.Logging;
import com.adobe.rhino.sdk.annotations.Prepare;
import com.adobe.rhino.sdk.annotations.Scenario;
import com.adobe.rhino.sdk.annotations.Simulation;
import com.adobe.rhino.sdk.annotations.UserFeeder;
import com.adobe.rhino.sdk.feeders.UUIDFeeder;
import com.adobe.rhino.sdk.reporting.GatlingLogFormatter;

/**
 * An example for annotated entity of benchmark job.
 */
@Simulation(name = "Test Simulation")
@Logging(file = "/var/tmp/simulation.log", formatter = GatlingLogFormatter.class)
public class RhinoEntity {

    @UserFeeder(max = 10)
    private User user;

    @Feeder(factory = UUIDFeeder.class)
    private String uuid;

    @Prepare
    public void prepare() {
        System.out.println("Preparing the test with user:" + user.getUsername());
    }

    @Scenario(name = "Hello World")
    public void run(Recorder recorder) {
        System.out.println("Running test with user:" + user.getUsername());
    }

    @CleanUp
    public void cleanUp() {
        System.out.println("Clean up the test.");
    }
}
