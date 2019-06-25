package io.ryos.rhino.sdk.feeders;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.repositories.UserRepository;

public class UserProvider implements Feedable<User> {

  private final UserRepository<UserSession> userRepository;

  public UserProvider(final UserRepository<UserSession> userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public User take() {
    return userRepository.take().getUser();
  }
}
