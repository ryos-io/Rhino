package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.SessionDslItem.Scope;

public interface ContainerScopeDsl<R> {

  UserSession collect(UserSession userSession, R response, String sessionKey,
      Scope sessionScope);

}
