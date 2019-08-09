package io.ryos.rhino.sdk.runners;

import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.exceptions.RhinoFrameworkError;
import io.ryos.rhino.sdk.reporting.Measurement;
import io.ryos.rhino.sdk.reporting.MeasurementImpl;
import io.ryos.rhino.sdk.reporting.UserEvent;
import io.ryos.rhino.sdk.reporting.UserEvent.EventType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.Callable;
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
  private final Object instance;

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
      final EventDispatcher eventDispatcher,
      final Object instance) {
    this.simulationMetadata = Objects.requireNonNull(simulationMetadata);
    this.userSession = Objects.requireNonNull(userSession);
    this.scenario = Objects.requireNonNull(scenario);
    this.eventDispatcher = Objects.requireNonNull(eventDispatcher);
    this.instance = instance;
  }

  @Override
  public Measurement call() {

    var user = userSession.getUser();

    // Before method call.
    executeMethod(simulationMetadata.getBeforeMethod(), instance, userSession);

    var measurement = new MeasurementImpl(scenario.getDescription(), user.getId());
    var start = System.currentTimeMillis();
    var userEventStart = new UserEvent(
        user.getUsername(),
        user.getId(),
        scenario.getDescription(),
        start,
        start,
        0,
        EventType.START,
        "",
        user.getId()
    );

    measurement.record(userEventStart);

    executeScenario(scenario, measurement, instance, userSession);

    var elapsed = System.currentTimeMillis() - start;
    var userEventEnd = new UserEvent(
        user.getUsername(),
        user.getId(),
        scenario.getDescription(),
        start,
        start + elapsed,
        elapsed,
        EventType.END,
        "",
        user.getId()
    );

    measurement.record(userEventEnd);

    eventDispatcher.dispatchEvents(measurement);

    // after call.
    executeMethod(simulationMetadata.getAfterMethod(), instance, userSession);

    return measurement;
  }

  private void executeScenario(final Scenario scenario,
      final MeasurementImpl measurement,
      final Object simulationInstance,
      final UserSession userSession) {
    try {
      var method = scenario.getMethod();
      var parameterTypes = method.getParameterTypes();

      // optimization might be required to avoid go through the conditions every time the scenario executed.
      if (parameterTypes.length == 0) {
        method.invoke(simulationInstance);
      } else if (parameterTypes.length == 2 &&
          parameterTypes[0].isAssignableFrom(Measurement.class) &&
          parameterTypes[1].isAssignableFrom(UserSession.class)) {
        method.invoke(simulationInstance, measurement, userSession);
      } else if (parameterTypes.length == 1 &&
          parameterTypes[0].isAssignableFrom(Measurement.class)) {
        method.invoke(simulationInstance, measurement);
      } else if (parameterTypes.length == 1 &&
          parameterTypes[0].isAssignableFrom(UserSession.class)) {
        method.invoke(simulationInstance, userSession);
      } else {
        throw new IllegalAccessException("No proper scenario method found.");
      }
    } catch (IllegalAccessException e) {
      LOG.error(e);
      throw new RhinoFrameworkError();
    } catch (InvocationTargetException e) {
      LOG.error(e.getTargetException());
    }
  }

  private void executeMethod(final Method method,
      final Object simulationInstance,
      final UserSession userSession) {
    try {
      if (method != null) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(UserSession.class)) {
          method.invoke(simulationInstance, userSession);
        } else if (parameterTypes.length == 0) {
          method.invoke(simulationInstance);
        } else {
          throw new IllegalAccessException("No proper scenario method found.");
        }
      }
    } catch (IllegalAccessException e) {
      LOG.error(e);
      throw new RhinoFrameworkError();
    } catch (InvocationTargetException e) {
      LOG.error(e.getTargetException());
    }
  }
}
