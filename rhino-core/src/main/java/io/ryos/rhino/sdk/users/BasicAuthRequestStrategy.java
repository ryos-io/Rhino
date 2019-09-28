package io.ryos.rhino.sdk.users;

import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.data.UserImpl;
import java.util.Base64;
import org.asynchttpclient.RequestBuilder;

public class BasicAuthRequestStrategy implements UserAuthRequestStrategy {
  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final String BASIC = "Basic ";
  private static final String USER = "user";
  private static final String SERVICE = "service";

  @Override
  public RequestBuilder addAuthHeaders(RequestBuilder builder, User user) {
    if (user instanceof UserImpl) {
      var userPass = String.format("%s:%s", user.getUsername(), user.getPassword());
      var token = Base64.getEncoder().encode(userPass.getBytes());
      builder = builder.addHeader(HEADER_AUTHORIZATION, BASIC + new String(token));
    }
    return builder;
  }
}
