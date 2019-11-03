package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.SimulationSession;
import io.ryos.rhino.sdk.users.data.User;

public class LoadToken {

  private final User user;
  private final SimulationSession simulationSession;

  public LoadToken(final User user, final SimulationSession simulationSession) {
    this.user = user;
    this.simulationSession = simulationSession;
  }

  public User getUser() {
    return user;
  }

  public SimulationSession getSimulationSession() {
    return simulationSession;
  }
}
