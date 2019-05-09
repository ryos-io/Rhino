/*
  Copyright 2018 Adobe.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

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
