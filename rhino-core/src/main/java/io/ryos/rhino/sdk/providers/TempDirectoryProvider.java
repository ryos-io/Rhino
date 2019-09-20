/*
 * Copyright 2018 Ryos.io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ryos.rhino.sdk.providers;

import io.ryos.rhino.sdk.exceptions.RhinoIOException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TempDirectoryProvider implements Provider<File> {

  @Override
  public File take() {
    try {
      return Files.createTempDirectory("rhino").toFile();
    } catch (IOException e) {
      throw new RhinoIOException("Cannot create a temp directory.", e);
    }
  }
}
