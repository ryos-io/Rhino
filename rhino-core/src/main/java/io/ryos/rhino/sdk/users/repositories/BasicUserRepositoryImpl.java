package io.ryos.rhino.sdk.users.repositories;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.data.UserSessionImpl;
import io.ryos.rhino.sdk.users.source.UserSource;
import java.util.List;
import java.util.stream.Collectors;

public class BasicUserRepositoryImpl implements UserRepository<UserSession> {

  private final UserSource userSource;

  public BasicUserRepositoryImpl(UserSource userSource) {
    this.userSource = userSource;
  }

  @Override
  public List<UserSession> leaseUsers(int numberOfUsers, String region) {
    var users = userSource.getUsers(numberOfUsers, region);
    return users.stream().map(UserSessionImpl::new).collect(Collectors.toList());
  }
}
