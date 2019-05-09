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

package com.adobe.rhino.sdk.data;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ContextImpl implements Context {

    // Data structure to store key value objects. The implementation employs the thread-safe
    // instance of HashMap.
    private final Map<String, Object> storage = new ConcurrentHashMap<>();

    public void add(String key, Object value) {
        storage.put(key, value);
    }

    public <T> Optional<T> get(String key) {
        if (storage.containsKey(key)) {
            return Optional.of((T) storage.get(key));
        }
        return Optional.empty();
    }

    public void empty() {
        storage.clear();
    }
}
