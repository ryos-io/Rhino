package io.ryos.rhino.sdk.runners;

import static io.ryos.rhino.sdk.utils.ReflectionUtils.getFieldByAnnotation;
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

  private SimulationMetadata simulationMetadata;

  private final UserSession userSession;
  private final Scenario scenario;
  private final EventDispatcher eventDispatcher;

  // Predicate to search fields for Provider annotation.
  private final Predicate<Field> hasFeeder = f -> Arrays
      .stream(f.getDeclaredAnnotations())
      .anyMatch(Provider.class::isInstance);

  private final Function<Field, InjectionPoint<Provider>> injectionPointFunction =
      f -> new InjectionPoint<>(f,
          f.getDeclaredAnnotation(Provider.class));

  // Provider the feeder value into the field.
  private void feed(final Object instance, final InjectionPoint<Provider> injectionPoint) {

    Objects.requireNonNull(instance, "Object instance is null.");
    var factoryInstance = instanceOf(injectionPoint.getAnnotation().factory()).orElseThrow();
    var value = factoryInstance.take();
    try {
      var field = injectionPoint.getField();
      field.setAccessible(true);
      //TODO pre-check before assignment.
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      LOG.error("Access to field failed.", e);
    } catch (IllegalArgumentException e) {
      LOG.error("Provider's return type and field's type is not compatible: " + e.getMessage());
    }
  }

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
  public DefaultSimulationCallable(final SimulationMetadata simulationMetadata,
      final UserSession userSession,
      final Scenario scenario,
      final EventDispatcher eventDispatcher) {
    this.simulationMetadata = Objects.requireNonNull(simulationMetadata);
    this.userSession = Objects.requireNonNull(userSession);
    this.scenario = Objects.requireNonNull(scenario);
    this.eventDispatcher = Objects.requireNonNull(eventDispatcher);
  }

  /**
   * Simulation object factory. All reflection calls should be run on this single instance.
   * <p>
   */
  private Supplier<Object> simulationInstanceFactory =
      () -> instanceOf(simulationMetadata.getSimulationClass()).orElseThrow();

  @Override
  public Measurement call() {

    var user = userSession.getUser();
    var simulationInstance = simulationInstanceFactory.get();

    new DefaultRunnerSimulationInjector(simulationMetadata, userSession).injectOn(simulationInstance);

    injectUser(user, simulationInstance);// Each thread will run as the same user.
    injectSession(userSession, simulationInstance);
    injectFeeders(simulationInstance);

    // Before method call.
    executeMethod(simulationMetadata.getBeforeMethod(), simulationInstance);

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

    executeScenario(scenario, measurement, simulationInstance);

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
    executeMethod(simulationMetadata.getAfterMethod(), simulationInstance);

    return measurement;
  }

  private void executeScenario(final Scenario scenario,
      final MeasurementImpl recorder,
      final Object simulationInstance) {
    try {
      scenario.getMethod().invoke(simulationInstance, recorder);
    } catch (IllegalAccessException | InvocationTargetException e) {
      LOG.error(e.getCause().getMessage());
      throw new RuntimeException(e.getCause());
    }
  }

  private void executeMethod(final Method method, final Object simulationInstance) {
    try {
      if (method != null) {
        method.invoke(simulationInstance);
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      LOG.error(e.getCause().getMessage());
      throw new RuntimeException(
          "Cannot invoke the method with step: " + method.getName() + "()", e);
    }
  }

  /* Find the first annotation type, clazzAnnotation, on field declarations of the clazz.  */
  private void injectFeeders(final Object simulationInstance) {
    Arrays.stream(simulationMetadata.getSimulationClass().getDeclaredFields())
        .filter(hasFeeder)
        .map(injectionPointFunction)
        .forEach(ip -> feed(simulationInstance, ip));
  }

  private void injectSession(final UserSession userSession, final Object simulationInstance) {
    var fieldAnnotation = getFieldByAnnotation(
        simulationMetadata.getSimulationClass(),
        SessionFeeder.class);
    fieldAnnotation
        .ifPresent(f -> setValueToInjectionPoint(userSession, f.getFirst(), simulationInstance));
  }

  private void injectUser(final User user, final Object simulationInstance) {
    var fieldAnnotation = getFieldByAnnotation(simulationMetadata.getSimulationClass(),
        UserProvider.class);
    fieldAnnotation
        .ifPresent(f -> setValueToInjectionPoint(user, f.getFirst(), simulationInstance));
  }

  private <T> void setValueToInjectionPoint(final T object, final Field f,
      final Object simulationInstance) {
    try {
      f.setAccessible(true);
      f.set(simulationInstance, object);
    } catch (IllegalAccessException e) {
      LOG.error(e);
      //TODO
    }
  }

  public void prepare(final UserSession userSession) {
    final Object cleanUpInstance = prepareMethodCall(userSession);
    executeMethod(simulationMetadata.getPrepareMethod(), cleanUpInstance);
  }

  private Object prepareMethodCall(final UserSession userSession) {
    final Object cleanUpInstance = simulationInstanceFactory.get();
    injectSession(userSession, cleanUpInstance);
    injectFeeders(cleanUpInstance);
    injectUser(userSession.getUser(), cleanUpInstance);
    return cleanUpInstance;
  }

  public void cleanUp(final UserSession userSession) {
    prepareMethodCall(userSession);
    executeMethod(simulationMetadata.getCleanupMethod(), userSession);
  }
}
