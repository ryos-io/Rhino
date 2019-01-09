package com.adobe.rhino.sdk.data;

import java.util.Optional;


/**
 * ContextImpl type for storing values throughout a testing session. Each session - and thread
 * respectfully, must have a single context instance bound.
 *
 * @author Erhan Bagdemir
 * @since 1.0
 */
public interface Context {
    /**
     * Puts a new key - value pair to the context.
     *
     * @param key   Key value.
     * @param value Value to store.
     */
    void add(String key, Object value);

    /**
     * Reclaims the object from the context.
     *
     * @param key The key value.
     * @param <T> The value stored in the context of type {@code T}
     * @return An {@link Optional} instance of {@code T}.
     */
    <T> Optional<T> get(String key);

    /**
     * Empties the context.
     */
    void empty();
}
