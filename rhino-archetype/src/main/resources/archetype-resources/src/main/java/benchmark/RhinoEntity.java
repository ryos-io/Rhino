/*
  Copyright 2018 Ryos.io.

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

import io.ryos.rhino.sdk.annotations.CleanUp;
import io.ryos.rhino.sdk.annotations.Feeder;
import io.ryos.rhino.sdk.annotations.Prepare;
import io.ryos.rhino.sdk.annotations.Scenario;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserFeeder;
import io.ryos.rhino.sdk.feeders.UUIDProvider;
import io.ryos.rhino.sdk.reporting.Measurement;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import javax.ws.rs.core.Response.Status;

/**
 * An example for annotated entity of benchmark job.
 */
@Simulation(name = "helloWorld")
public class RhinoEntity {

    private static final long WAIT_FOR = 500L;

    @UserFeeder(max = 10, factory = OAuthUserRepositoryFactoryImpl.class)
    private User user;

    @Feeder(factory = UUIDProvider.class)
    private String uuid;

    @Prepare
    public void prepare() {
        System.out.println("Preparing the test with user:" + user.getUsername());
    }

    @Scenario(name = "hello")
    public void testHelloWorld(Measurement measurement) {
        System.out.println(uuid + " Hello World! Running test with user:" + user.getUsername());
        waitFor(WAIT_FOR);
        measurement.measure("Request", Status.OK.toString());
    }

    private void waitFor(long waitFor) {
        try {
            Thread.sleep(waitFor);
        } catch (InterruptedException e) {
            //
        }
    }

    @CleanUp
    public void cleanUp() {
        System.out.println("Clean up the test.");
    }
}
