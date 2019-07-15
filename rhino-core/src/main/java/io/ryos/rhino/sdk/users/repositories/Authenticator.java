package io.ryos.rhino.sdk.users.repositories;

import io.ryos.rhino.sdk.users.data.User;

/**
 * Authenticator for users.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface Authenticator<T extends User> {

  /**
   * Authenticates a user against an authorization server.
   * <p>
   *
   * @param user The user to be authenticated.
   * @return Authenticated user type. In OAuth 2.0 user context, it is a
   * {@link io.ryos.rhino.sdk.users.data.OAuthUser}.
   */
  T authenticate(User user);

}
