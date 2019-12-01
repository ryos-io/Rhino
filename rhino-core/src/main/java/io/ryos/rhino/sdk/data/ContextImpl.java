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

package io.ryos.rhino.sdk.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ContextImpl implements Context {

  // Data structure to store key value objects. The implementation employs the thread-safe
  // instance of HashMap.
  private final Map<String, Object> storage = new ConcurrentHashMap<>();

  public Context add(String key, Object value) {
    storage.put(key, value);
    return this;
  }

  public <T> Context addAll(String key, T... objects) {
    if (!storage.containsKey(key)) {
      storage.put(key, Arrays.asList(objects));
      return this;
    }
    if (storage.containsKey(key) && storage.get(key) instanceof Collection) {
      ((Collection<T>) storage.get(key)).addAll(Arrays.asList(objects));
    }

    return this;
  }

  public <T> Optional<T> get(String key) {
    if (storage.containsKey(Objects.requireNonNull(key, "session key may not be null."))) {
      return Optional.of((T) storage.get(key));
    }
    return Optional.empty();
  }

  public void empty() {
    storage.clear();
  }

  @Override
  public boolean isEmpty() {
    return storage.isEmpty();
  }
}
