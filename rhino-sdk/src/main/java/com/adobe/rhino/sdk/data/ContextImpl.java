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
