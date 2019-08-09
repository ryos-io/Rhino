package io.ryos.rhino.sdk.runners;


import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.reporting.Measurement;
import io.ryos.rhino.sdk.users.data.UserImpl;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    var throwingExceptionDummySimulation = new ThrowingExceptionDummySimulation();
    var scenarioMethod = new Scenario("N/A",
        throwingExceptionDummySimulation.getClass().getMethod("scenarioMethod", UserSession.class));
    var defaultSimulationCallable = new DefaultSimulationCallable(
        simulationMetadata, userSession, scenarioMethod, eventDispatcher, throwingExceptionDummySimulation);

    when(simulationMetadata.getBeforeMethod()).thenReturn(throwingExceptionDummySimulation.getClass().getMethod("before", UserSession.class));
    when(simulationMetadata.getAfterMethod()).thenReturn(throwingExceptionDummySimulation.getClass().getMethod("after", UserSession.class));

    when(userSession.getUser()).thenReturn(new UserImpl("username", "pw", "id", "openid"));
    final Measurement call = defaultSimulationCallable.call();

    verify(userSession).add("before","called");
    verify(userSession).add("after","called");

    assertThat(call, notNullValue());
  }

  @Test
  public void testCall() throws NoSuchMethodException {
    var dummySimulation = new DummySimulation();
    var scenarioMethod = new Scenario("N/A",
            dummySimulation.getClass().getMethod("scenarioMethod", UserSession.class));
    var defaultSimulationCallable = new DefaultSimulationCallable(
            simulationMetadata, userSession, scenarioMethod, eventDispatcher, dummySimulation);

    when(simulationMetadata.getBeforeMethod()).thenReturn(dummySimulation.getClass().getMethod("before", UserSession.class));
    when(simulationMetadata.getAfterMethod()).thenReturn(dummySimulation.getClass().getMethod("after", UserSession.class));

    when(userSession.getUser()).thenReturn(new UserImpl("username", "pw", "id", "openid"));
    final Measurement call = defaultSimulationCallable.call();

    verify(userSession).add("before","called");
    verify(userSession).add("after","called");
    verify(userSession).add("scenario","called");

    assertThat(call, notNullValue());
  }

  @Test
  public void testCallWithoutScenarioArgument() throws NoSuchMethodException {
    var dummySimulation = new DummySimulation();
    var scenarioMethod = new Scenario("N/A",
            dummySimulation.getClass().getMethod("scenarioMethod2"));
    var defaultSimulationCallable = new DefaultSimulationCallable(
            simulationMetadata, userSession, scenarioMethod, eventDispatcher, dummySimulation);

    when(simulationMetadata.getBeforeMethod()).thenReturn(dummySimulation.getClass().getMethod("before", UserSession.class));
    when(simulationMetadata.getAfterMethod()).thenReturn(dummySimulation.getClass().getMethod("after", UserSession.class));

    when(userSession.getUser()).thenReturn(new UserImpl("username", "pw", "id", "openid"));
    final Measurement call = defaultSimulationCallable.call();

    verify(userSession).add("before","called");
    verify(userSession).add("after","called");

    assertThat(call, notNullValue());
  }

  @Test
  public void testCallMeasurementSession() throws NoSuchMethodException {
    var dummySimulation = new DummySimulation();
    var scenarioMethod = new Scenario("N/A",
            dummySimulation.getClass().getMethod("scenarioMethod3", Measurement.class, UserSession.class));
    var defaultSimulationCallable = new DefaultSimulationCallable(
            simulationMetadata, userSession, scenarioMethod, eventDispatcher, dummySimulation);

    when(simulationMetadata.getBeforeMethod()).thenReturn(dummySimulation.getClass().getMethod("before", UserSession.class));
    when(simulationMetadata.getAfterMethod()).thenReturn(dummySimulation.getClass().getMethod("after", UserSession.class));

    when(userSession.getUser()).thenReturn(new UserImpl("username", "pw", "id", "openid"));
    final Measurement call = defaultSimulationCallable.call();

    verify(userSession).add("before","called");
    verify(userSession).add("after","called");
    verify(userSession).add("scenario","called");

    assertThat(call, notNullValue());
  }

  @Test
  public void testCallBeforeThrowingException() throws NoSuchMethodException {
    var dummySimulation = new BeforeThrowingExceptionDummySimulation();
    var scenarioMethod = new Scenario("N/A",
            dummySimulation.getClass().getMethod("scenarioMethod", UserSession.class));
    var defaultSimulationCallable = new DefaultSimulationCallable(
            simulationMetadata, userSession, scenarioMethod, eventDispatcher, dummySimulation);

    when(simulationMetadata.getBeforeMethod()).thenReturn(dummySimulation.getClass().getMethod("before", UserSession.class));
    when(simulationMetadata.getAfterMethod()).thenReturn(dummySimulation.getClass().getMethod("after", UserSession.class));

    when(userSession.getUser()).thenReturn(new UserImpl("username", "pw", "id", "openid"));
    final Measurement call = defaultSimulationCallable.call();

    verify(userSession).add("before","called");
    verify(userSession).add("after","called");
    verify(userSession).add("scenario","called");

    assertThat(call, notNullValue());
  }

  @Test
  public void testCallAfterThrowingException() throws NoSuchMethodException {
    var dummySimulation = new AfterThrowingExceptionDummySimulation();
    var scenarioMethod = new Scenario("N/A",
            dummySimulation.getClass().getMethod("scenarioMethod", UserSession.class));
    var defaultSimulationCallable = new DefaultSimulationCallable(
            simulationMetadata, userSession, scenarioMethod, eventDispatcher, dummySimulation);

    when(simulationMetadata.getBeforeMethod()).thenReturn(dummySimulation.getClass().getMethod("before", UserSession.class));
    when(simulationMetadata.getAfterMethod()).thenReturn(dummySimulation.getClass().getMethod("after", UserSession.class));

    when(userSession.getUser()).thenReturn(new UserImpl("username", "pw", "id", "openid"));
    final Measurement call = defaultSimulationCallable.call();

    verify(userSession).add("before","called");
    verify(userSession).add("after","called");
    verify(userSession).add("scenario","called");

    assertThat(call, notNullValue());
  }

  @Simulation(name = "Dummy")
  class AfterThrowingExceptionDummySimulation {

    @io.ryos.rhino.sdk.annotations.Before
    public void before(UserSession session) {
      session.add("before", "called");
    }

    @io.ryos.rhino.sdk.annotations.After
    public void after(UserSession session) {
      session.add("after", "called");
      throw new RuntimeException("Expected Exception, must be discarded");
    }

    @io.ryos.rhino.sdk.annotations.Scenario(name="test")
    public void scenarioMethod(UserSession session) {
      session.add("scenario", "called");
    }
  }

  @Simulation(name = "Dummy")
  class BeforeThrowingExceptionDummySimulation {

    @io.ryos.rhino.sdk.annotations.Before
    public void before(UserSession session) {
      session.add("before", "called");
      throw new RuntimeException("Expected Exception, must be discarded");
    }

    @io.ryos.rhino.sdk.annotations.After
    public void after(UserSession session) {
      session.add("after", "called");
    }

    @io.ryos.rhino.sdk.annotations.Scenario(name="test")
    public void scenarioMethod(UserSession session) {
      session.add("scenario", "called");
    }
  }

  @Simulation(name = "Dummy")
  class ThrowingExceptionDummySimulation {

    @io.ryos.rhino.sdk.annotations.Before
    public void before(UserSession session) {
      session.add("before", "called");
    }

    @io.ryos.rhino.sdk.annotations.After
    public void after(UserSession session) {
      session.add("after", "called");
    }

    @io.ryos.rhino.sdk.annotations.Scenario(name="test")
    public void scenarioMethod(UserSession session) {
      throw new RuntimeException("Expected Exception, must be discarded");
    }
  }

  @Simulation(name = "Dummy")
  class DummySimulation {

    @io.ryos.rhino.sdk.annotations.Before
    public void before(UserSession session) {
      session.add("before", "called");
    }

    @io.ryos.rhino.sdk.annotations.After
    public void after(UserSession session) {
      session.add("after", "called");
    }

    @io.ryos.rhino.sdk.annotations.Scenario(name="test")
    public void scenarioMethod3(Measurement measurement, UserSession session) {
      session.add("scenario", "called");
    }

    @io.ryos.rhino.sdk.annotations.Scenario(name="test")
    public void scenarioMethod2() {
    }

    @io.ryos.rhino.sdk.annotations.Scenario(name="test")
    public void scenarioMethod(UserSession session) {
      session.add("scenario", "called");
    }
  }
}
