package io.ryos.rhino.sdk.providers;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.exceptions.NoUserFoundException;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.oauth.OAuthUser;
import io.ryos.rhino.sdk.users.repositories.CyclicUserSessionRepository;

/**
 * OAuth user provider is to use to inject secondary users in addition to one which is provided
 * by the framework, that is the primary one.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class OAuthUserProvider implements Provider<OAuthUser> {

  private static final int MAX_ATTEMPT = 4;

  /**
   * {@link CyclicUserSessionRepository} instance provides a cyclic list of user sessions.
   * <p>
   */
  private final CyclicUserSessionRepository<UserSession> userSessionRepository;

  /**
   * Constructs a new {@link OAuthUserProvider} instance.
   * <p>
   *
   * @param userSessionRepository {@link CyclicUserSessionRepository} instance.
   */
  public OAuthUserProvider(final CyclicUserSessionRepository<UserSession> userSessionRepository) {
    this.userSessionRepository = userSessionRepository;
  }

  @Override
  public OAuthUser take() {

    var user = userSessionRepository.take().getUser();
    if (user instanceof OAuthUser) {
      return (OAuthUser) userSessionRepository.take().getUser();
    }

    throw new NoUserFoundException("No OAuth users found in the repository.");
  }

  /**
   * Take a user from the repository excluding the one passed as parameter till the repository
   * provides one. If after three attempts not succeeds, then {@link IllegalArgumentException}
   * will be thrown.
   * <p>
   *
   * @param excludedUser User instance.
   * @return {@link OAuthUser} instance different than the user provided.
   */
  public OAuthUser take(User excludedUser) {

    int attempt = 0;
    while (attempt++ < MAX_ATTEMPT) {
      var userTaken = take();
      if (!userTaken.equals(excludedUser)) {
        return userTaken;
      }
    }

    throw new NoUserFoundException("No OAuth users found in the repository or after 3 attempts "
        + "no other user found except the provided one.");
  }
}
