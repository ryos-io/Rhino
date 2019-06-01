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

package io.ryos.rhino.sdk.runners;

import io.ryos.rhino.sdk.data.Context;
import org.apache.commons.lang3.NotImplementedException;

// Dummy. Not yet implemented.
public class ReactiveSimulationRunner implements SimulationRunner {

  private final Context context;

  public ReactiveSimulationRunner(Context context) {
    this.context = context;
  }

  @Override
  public void start() {
    throw new NotImplementedException("Reactive runner is not yet implemented.");
  }

  @Override
  public void stop() {
    throw new NotImplementedException("Reactive runner is not yet implemented.");
  }
}
