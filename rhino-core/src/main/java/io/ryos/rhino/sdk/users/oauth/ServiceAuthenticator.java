package io.ryos.rhino.sdk.users.oauth;

public interface ServiceAuthenticator extends Authenticator<OAuthService, OAuthService> {

  /**
   * Authenticates a service against an authorization server.
   * <p>
   *
   * @param user The service to be authenticated.
   * @return Authenticated service type.
   */
  OAuthService authenticate(OAuthService user);
}
