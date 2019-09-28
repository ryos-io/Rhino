package io.ryos.rhino.sdk.users.oauth;

import javax.ws.rs.core.Response;

/**
 * Deserializes the response object into framework DTOs.
 *
 * @param <T> Target DTO type.
 * @author Erhan Bagdemir
 * @since 1.8.0
 */
public interface ResponseDeserializer<T> {

  /**
   * Deserializes the response object into framework DTOs.
   *
   * @param response Response object.
   * @return DTO instance.
   */
  T deserialize(Response response);
}
