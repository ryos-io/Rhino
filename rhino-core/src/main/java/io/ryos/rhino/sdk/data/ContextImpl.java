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

import io.ryos.rhino.sdk.reporting.Measurement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ContextImpl implements Context {

  // Data structure to store key value objects. The implementation employs the thread-safe
  // instance of HashMap.
  private final Map<String, Object> storage = new ConcurrentHashMap<>();
  private final List<Measurement> measurements = new ArrayList<>();

  public Context add(String key, Object value) {
    storage.put(key, value);
    return this;
  }

  public <T> Optional<T> get(String key) {
    if (storage.containsKey(Objects.requireNonNull(key, "define key may not be null."))) {
      return Optional.<T>of((T) storage.<T>get(key));
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

  @Override
  public void register(Measurement measurement) {
    measurements.add(measurement);
  }

  @Override
  public void notify(long time) {
    measurements.forEach(m -> m.add(time));
  }

  @Override
  public void commit(String status) {
    measurements.forEach(m -> m.commit(status));
  }

  @Override
  public void remove(Measurement measurement) {
    measurements.remove(measurement);
  }
}
