package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.SimulationSession;
import io.ryos.rhino.sdk.users.data.User;

public class LoadToken {

  private final User userSession;
  private final SimulationSession simulationSession;

  public LoadToken(final User userSession,
      final SimulationSession simulationSession) {
    this.userSession = userSession;
    this.simulationSession = simulationSession;
  }

  public User getUser() {
    return userSession;
  }

  public SimulationSession getSimulationSession() {
    return simulationSession;
  }
}
