package io.ryos.rhino.sdk.feeders;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.users.data.OAuthUser;
import io.ryos.rhino.sdk.users.repositories.RegionalUserProvider;

public class OAuthUserProvider implements Provider<OAuthUser> {

  private final RegionalUserProvider<UserSession> userProvider;

  public OAuthUserProvider(final RegionalUserProvider<UserSession> userProvider) {
    this.userProvider = userProvider;
  }

  @Override
  public OAuthUser take() {
    return (OAuthUser) userProvider.take().getUser();
  }
}
