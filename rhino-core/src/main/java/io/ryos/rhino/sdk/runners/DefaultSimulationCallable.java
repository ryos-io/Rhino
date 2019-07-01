package io.ryos.rhino.sdk.runners;

import static io.ryos.rhino.sdk.utils.ReflectionUtils.getFieldsByAnnotation;
import static io.ryos.rhino.sdk.utils.ReflectionUtils.instanceOf;

import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.annotations.Provider;
import io.ryos.rhino.sdk.annotations.SessionFeeder;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.data.InjectionPoint;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.reporting.Measurement;
import io.ryos.rhino.sdk.reporting.MeasurementImpl;
import io.ryos.rhino.sdk.reporting.UserEvent;
import io.ryos.rhino.sdk.reporting.UserEvent.EventType;
import io.ryos.rhino.sdk.users.data.User;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This callable will be called by the pipeline every time a new item is emitted.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.0.0
 */
public class DefaultSimulationCallable implements Callable<Measurement> {

  private static final Logger LOG = LogManager.getLogger(DefaultSimulationCallable.class);

  private final SimulationMetadata simulationMetadata;
  private final UserSession userSession;
  private final Scenario scenario;
  private final EventDispatcher eventDispatcher;

  /**
   * Instantiates a new {@link DefaultSimulationCallable} instance.
   * <p>
   *
   * @param simulationMetadata Simulation metadata.
   * @param userSession Current user session which is active.
   * @param scenario Scenario is to be run.
   * @param eventDispatcher Event dispatcher is responsible from delivering metric events to
   * corresponding receivers.
   */
  DefaultSimulationCallable(final SimulationMetadata simulationMetadata,
      final UserSession userSession,
      final Scenario scenario,
      final EventDispatcher eventDispatcher) {
    this.simulationMetadata = Objects.requireNonNull(simulationMetadata);
    this.userSession = Objects.requireNonNull(userSession);
    this.scenario = Objects.requireNonNull(scenario);
    this.eventDispatcher = Objects.requireNonNull(eventDispatcher);
  }

  @Override
  public Measurement call() {

    var user = userSession.getUser();

    // Before method call.
    executeMethod(simulationMetadata.getBeforeMethod(), simulationMetadata.getTestInstance(),
        userSession);

    var measurement = new MeasurementImpl(scenario.getDescription(), user.getId());
    var start = System.currentTimeMillis();
    var userEventStart = new UserEvent();
    userEventStart.elapsed = 0;
    userEventStart.start = start;
    userEventStart.end = start;
    userEventStart.scenario = scenario.getDescription();
    userEventStart.eventType = EventType.START;
    userEventStart.id = user.getId();

    measurement.record(userEventStart);

    executeScenario(scenario, measurement, simulationMetadata.getTestInstance(), userSession);

    var elapsed = System.currentTimeMillis() - start;
    var userEventEnd = new UserEvent();
    userEventEnd.elapsed = elapsed;
    userEventEnd.start = start;
    userEventEnd.end = start + elapsed;
    userEventEnd.scenario = scenario.getDescription();
    userEventEnd.eventType = EventType.END;
    userEventEnd.id = user.getId();
    measurement.record(userEventEnd);

    eventDispatcher.dispatchEvents(measurement);
    executeMethod(simulationMetadata.getAfterMethod(), simulationMetadata.getTestInstance(),
        userSession);

    return measurement;
  }

  private void executeScenario(final Scenario scenario,
      final MeasurementImpl recorder,
      final Object simulationInstance,
      final UserSession userSession) {
    try {
      scenario.getMethod().invoke(simulationInstance, recorder, userSession);
    } catch (IllegalAccessException | InvocationTargetException e) {
      LOG.error(e.getCause().getMessage());
      throw new RuntimeException(e.getCause());
    }
  }

  private void executeMethod(final Method method,
      final Object simulationInstance,
      final UserSession userSession) {
    try {
      if (method != null) {
        method.invoke(simulationInstance, userSession);
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      LOG.error(e.getCause().getMessage());
      throw new RuntimeException(
          "Cannot invoke the method with step: " + method.getName() + "()", e);
    }
  }
}
