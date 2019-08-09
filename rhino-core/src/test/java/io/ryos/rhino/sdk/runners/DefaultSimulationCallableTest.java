package io.ryos.rhino.sdk.runners;


import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.reporting.Measurement;
import io.ryos.rhino.sdk.users.data.UserImpl;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class DefaultSimulationCallableTest {

  @Mock
  EventDispatcher eventDispatcher;

  @Mock
  SimulationMetadata simulationMetadata;

  @Mock
  UserSession userSession;

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void testCallWithException() throws NoSuchMethodException {
    final SimulationDummy simulationDummy = new SimulationDummy();

    final Scenario scenarioMethod = new Scenario("N/A",
        simulationDummy.getClass().getMethod("scenarioMethod"));
    final DefaultSimulationCallable defaultSimulationCallable = new DefaultSimulationCallable(
        simulationMetadata, userSession, scenarioMethod, eventDispatcher, simulationDummy);

    when(userSession.getUser()).thenReturn(new UserImpl("username", "pw", "id", "openid"));
    final Measurement call = defaultSimulationCallable.call();

    assertThat(call, notNullValue());
  }

  class SimulationDummy {

    public void scenarioMethod() {
      throw new RuntimeException("Expected Exception, must be discarded");
    }
  }
}
