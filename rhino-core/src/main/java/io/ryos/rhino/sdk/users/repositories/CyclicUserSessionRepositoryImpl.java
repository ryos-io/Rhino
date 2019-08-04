/*
 * Copyright 2018 Ryos.io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ryos.rhino.sdk.users.repositories;

import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.data.SimulationSession;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.data.UserSessionImpl;
import io.ryos.rhino.sdk.dsl.LoadToken;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * User session repository provides the user sessions limited by maxNumberOfUsers. The take() method returns the next
 * user session in the backing collection. Once all elements in the collection are returned, the cyclic iterator starts
 * from the beginning.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class CyclicUserSessionRepositoryImpl implements CyclicUserSessionRepository<UserSession> {

  private static final int MAX_NUMBER_OF_USERS = 1000;

  private final CyclicIterator<LoadToken> filteredIterator;
  private final List<LoadToken> backingList;

  public CyclicUserSessionRepositoryImpl(
      final UserRepository<UserSession> userRepository,
      final String region) {

    this(userRepository, region, MAX_NUMBER_OF_USERS);
  }

  public CyclicUserSessionRepositoryImpl(
      final UserRepository<UserSession> userRepository,
      final String region,
      final int maxNumberOfUsers) {

    Objects.requireNonNull(userRepository);
    Objects.requireNonNull(region);

    var filteredUsers = userRepository.leaseUsers(maxNumberOfUsers, region);
    this.backingList = filteredUsers
        .stream()
        .map(u -> new LoadToken(u.getUser(), new SimulationSession(u.getUser())))
        .collect(Collectors.toList());

    this.filteredIterator = new CyclicIterator<>(backingList);
  }


  @Override
  public UserSession take() {
    final LoadToken token = filteredIterator.next();
    return new UserSessionImpl(token.getUser(), token.getSimulationSession());
  }

  public List<UserSession> getUserList() {
    return backingList.stream().map(token ->
        new UserSessionImpl(token.getUser(), token.getSimulationSession()))
        .collect(Collectors.toList());
  }

  public List<LoadToken> getTokenList() {
    return backingList;
  }
}
