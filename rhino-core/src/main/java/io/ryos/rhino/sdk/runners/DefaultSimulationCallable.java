package io.ryos.rhino.sdk.runners;

import static io.ryos.rhino.sdk.utils.ReflectionUtils.getFieldByAnnotation;
import static io.ryos.rhino.sdk.utils.ReflectionUtils.instanceOf;

import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.annotations.Feeder;
import io.ryos.rhino.sdk.annotations.SessionFeeder;
import io.ryos.rhino.sdk.annotations.UserFeeder;
import io.ryos.rhino.sdk.data.InjectionPoint;
import io.ryos.rhino.sdk.data.Pair;
import io.ryos.rhino.sdk.data.Scenario;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.feeders.Feedable;
import io.ryos.rhino.sdk.reporting.Measurement;
import io.ryos.rhino.sdk.reporting.MeasurementImpl;
import io.ryos.rhino.sdk.reporting.UserEvent;
import io.ryos.rhino.sdk.reporting.UserEvent.EventType;
import io.ryos.rhino.sdk.users.data.User;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default callable for push approach.
 */
public class DefaultSimulationCallable implements Callable<Measurement> {

  private static final Logger LOG = LogManager.getLogger(DefaultSimulationCallable.class);

  private SimulationMetadata simulationMetadata;
  private UserSession userSession;
  private Scenario scenario;

  // Predicate to search fields for Feedable annotation.
  private final Predicate<Field> hasFeeder = f -> Arrays
      .stream(f.getDeclaredAnnotations())
      .anyMatch(io.ryos.rhino.sdk.annotations.Feeder.class::isInstance);

  private final Function<Field, InjectionPoint<Feeder>> injectionPointFunction =
      f -> new InjectionPoint<>(f,
          f.getDeclaredAnnotation(io.ryos.rhino.sdk.annotations.Feeder.class));

  // Feedable the feeder value into the field.
  private void feed(final Object instance,
      final InjectionPoint<io.ryos.rhino.sdk.annotations.Feeder> ip) {
    Feedable o = instanceOf(ip.getAnnotation().factory()).orElseThrow();
    Object value = o.take();
    try {
      Field field = ip.getField();
      field.setAccessible(true);
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      LOG.error("Access to field failed.", e);
    } catch (IllegalArgumentException e) {
      LOG.error("Feedable's return type and field's type is not compatible: " + e.getMessage());
    }
  }

  public DefaultSimulationCallable(final SimulationMetadata simulationMetadata,
      final UserSession userSession, final Scenario scenario) {
    this.simulationMetadata = simulationMetadata;
    this.userSession = userSession;
    this.scenario = scenario;
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

    injectUser(user, simulationInstance);// Each thread will run as the same user.
    injectSession(userSession, simulationInstance);

    executeMethod(simulationMetadata.getBeforeMethod(), simulationInstance);

    feedInjections(simulationInstance);

    var recorder = new MeasurementImpl(scenario.getDescription(), user.getId());
    var start = System.currentTimeMillis();
    var userEventStart = new UserEvent();
    userEventStart.elapsed = 0;
    userEventStart.start = start;
    userEventStart.end = start;
    userEventStart.scenario = scenario.getDescription();
    userEventStart.eventType = EventType.START;
    userEventStart.id = user.getId();

    recorder.record(userEventStart);

    executeScenario(scenario, recorder, simulationInstance);

    var elapsed = System.currentTimeMillis() - start;

    var userEventEnd = new UserEvent();
    userEventEnd.elapsed = elapsed;
    userEventEnd.start = start;
    userEventEnd.end = start + elapsed;
    userEventEnd.scenario = scenario.getDescription();
    userEventEnd.eventType = EventType.END;
    userEventEnd.id = user.getId();
    recorder.record(userEventEnd);

    EventDispatcher.instance(simulationMetadata).dispatchEvents(recorder);
    executeMethod(simulationMetadata.getAfterMethod(), simulationInstance);

    return recorder;
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
  private void feedInjections(Object simulationInstance) {
    Arrays.stream(simulationMetadata.getSimulationClass().getDeclaredFields())
        .filter(hasFeeder)
        .map(injectionPointFunction)
        .forEach(ip -> feed(simulationInstance, ip));
  }

  private void injectSession(final UserSession userSession, final Object simulationInstance) {
    final Optional<Pair<Field, SessionFeeder>> fieldAnnotation = getFieldByAnnotation(
        simulationMetadata.getSimulationClass(),
        SessionFeeder.class);
    fieldAnnotation
        .ifPresent(f -> setValueToInjectionPoint(userSession, f.getFirst(), simulationInstance));
  }

  private void injectUser(final User user, final Object simulationInstance) {
    final Optional<Pair<Field, UserFeeder>> fieldAnnotation = getFieldByAnnotation(
        simulationMetadata.getSimulationClass(),
        UserFeeder.class);
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


  public void prepare(UserSession userSession) {
    final Object cleanUpInstance = prepareMethodCall(userSession);
    executeMethod(simulationMetadata.getPrepareMethod(), cleanUpInstance);
  }

  private Object prepareMethodCall(final UserSession userSession) {
    final Object cleanUpInstance = simulationInstanceFactory.get();
    injectSession(userSession, cleanUpInstance);
    feedInjections(cleanUpInstance);
    injectUser(userSession.getUser(), cleanUpInstance);
    return cleanUpInstance;
  }

  public void cleanUp(UserSession userSession) {
    prepareMethodCall(userSession);
    executeMethod(simulationMetadata.getCleanupMethod(), userSession);
  }
}
