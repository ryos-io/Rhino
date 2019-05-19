package io.ryos.rhino.sdk.validators;

/**
 * @author Erhan Bagdemir
 * @since 1.0
 */
public interface Validator<T> {

  void validate(T props);
}
