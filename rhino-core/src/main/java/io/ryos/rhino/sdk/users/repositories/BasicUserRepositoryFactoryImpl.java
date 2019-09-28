package io.ryos.rhino.sdk.users.repositories;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.users.source.UserSource;

public class BasicUserRepositoryFactoryImpl implements UserRepositoryFactory<UserSession> {

  @Override
  public UserRepository<UserSession> create() {
    return new BasicUserRepositoryImpl(UserSource.newSource());
  }
}
