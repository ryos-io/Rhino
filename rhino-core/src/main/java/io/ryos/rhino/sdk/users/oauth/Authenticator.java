package io.ryos.rhino.sdk.users.oauth;

/**
 * Generic authenticator authenticates an entity against an authorization server. The entity can
 * be a user or a service.
 * <p>
 *
 * @param <T> The entity type being authenticated.
 * @param <R> Return type with authentication tokens. The return type is to be a sub type of the
 * entity.
 * @author Erhan Bagdemir
 * @since 1.6.0
 */
public interface Authenticator<T, R extends T> {

  /**
   * Authenticates an entity against an authorization server. The entity can be a user or a
   * service.
   * <p>
   *
   * @param entity The entity to be authenticated.
   * @return Authenticated entity type which has is-a relationship to the entity type being
   * authenticated.
   */
  R authenticate(T entity);
}
