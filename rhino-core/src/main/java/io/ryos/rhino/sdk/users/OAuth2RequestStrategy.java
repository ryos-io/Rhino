package io.ryos.rhino.sdk.users;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.exceptions.UnknownTokenTypeException;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.oauth.OAuthUser;
import org.asynchttpclient.RequestBuilder;

public class OAuth2RequestStrategy implements UserAuthRequestStrategy {
  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final String BEARER = "Bearer ";
  private static final String USER = "user";
  private static final String SERVICE = "service";

  @Override
  public RequestBuilder addAuthHeaders(RequestBuilder builder, User user) {

    if (user instanceof OAuthUser) {
      var authService = ((OAuthUser) user).getOAuthService();
      if (SimulationConfig.isServiceAuthenticationEnabled()) {
        var serviceAccessToken = authService.getAccessToken();
        var userToken = ((OAuthUser) user).getAccessToken();

        if (USER.equals(SimulationConfig.getBearerType())) {
          builder = builder.addHeader(HEADER_AUTHORIZATION, BEARER + userToken);
          builder = builder.addHeader(SimulationConfig.getHeaderName(),
              BEARER + serviceAccessToken);
        } else if (SERVICE.equals(SimulationConfig.getBearerType())) {
          builder = builder.addHeader(HEADER_AUTHORIZATION, BEARER + serviceAccessToken);
          builder = builder.addHeader(SimulationConfig.getHeaderName(), BEARER + userToken);
        } else {
          throw new UnknownTokenTypeException(SimulationConfig.getBearerType());
        }

      } else {
        var token = ((OAuthUser) user).getAccessToken();
        builder = builder.addHeader(HEADER_AUTHORIZATION, BEARER + token);
      }
    }
    return builder;
  }
}
