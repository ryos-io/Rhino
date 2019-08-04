package io.ryos.rhino.sdk.providers;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.users.oauth.OAuthUser;
import io.ryos.rhino.sdk.users.repositories.CyclicUserSessionRepository;

public class OAuthUserProvider implements Provider<OAuthUser> {

  private final CyclicUserSessionRepository<UserSession> userProvider;

  public OAuthUserProvider(final CyclicUserSessionRepository<UserSession> userProvider) {
    this.userProvider = userProvider;
  }

  @Override
  public OAuthUser take() {

    var user = userProvider.take().getUser();
    if (user instanceof OAuthUser) {
      return (OAuthUser) userProvider.take().getUser();
    }

    throw new IllegalArgumentException("No OAuth users found in the repository.");
  }
}
