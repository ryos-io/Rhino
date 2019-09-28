package io.ryos.rhino.sdk.users;

import io.ryos.rhino.sdk.users.data.User;
import org.asynchttpclient.RequestBuilder;

public interface UserAuthRequestStrategy {

  public RequestBuilder addAuthHeaders(RequestBuilder builder, User user);

}
