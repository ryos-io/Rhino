package io.ryos.rhino.sdk.runners;

import static io.ryos.rhino.sdk.reporting.GatlingSimulationLogFormatter.GATLING_HEADLINE_TEMPLATE;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import io.ryos.rhino.sdk.Simulation;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.io.InfluxDBWriter;
import io.ryos.rhino.sdk.io.SimulationLogWriter;
import io.ryos.rhino.sdk.reporting.GatlingSimulationLogFormatter;
import io.ryos.rhino.sdk.reporting.MeasurementImpl;
import io.ryos.rhino.sdk.reporting.StdoutReporter;
import io.ryos.rhino.sdk.reporting.StdoutReporter.EndTestEvent;
import java.time.Instant;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.concurrent.Await;
import scala.concurrent.duration.FiniteDuration;

/**
 * Singleton event dispatcher forwards the events created by simulation callables to
 * corresponding entities like actors wnich process them.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class EventDispatcher {

  private static final Logger LOG = LogManager.getLogger(EventDispatcher.class);
  private static final long TERMINATION_REQUEST_TIMEOUT = 5000L;
  private static final String ACTOR_SYS_NAME = "rhino-dispatcher";

  private static EventDispatcher INSTANCE;

  private EventDispatcher(final SimulationMetadata simulationMetadata) {

    this.simulationMetadata = Objects.requireNonNull(simulationMetadata);
    this.stdOutReptorter = system
        .actorOf(StdoutReporter.props(simulationMetadata.getNumberOfUsers(),
            Instant.now(),
            simulationMetadata.getDuration()),
            StdoutReporter.class.getName());
    this.loggerActor = system
        .actorOf(SimulationLogWriter.props(simulationMetadata.getReportingURI(),
            simulationMetadata.getLogFormatter()),
            SimulationLogWriter.class.getName());

    if (simulationMetadata.isEnableInflux()) {
      influxActor = system.actorOf(InfluxDBWriter.props(), InfluxDBWriter.class.getName());
    }

    if (simulationMetadata.getLogFormatter() instanceof GatlingSimulationLogFormatter) {
      loggerActor.tell(
          String.format(
              GATLING_HEADLINE_TEMPLATE,
              simulationMetadata.getSimulationClass().getName(),
              simulationMetadata.getSimulationName(),
              System.currentTimeMillis(),
              GatlingSimulationLogFormatter.GATLING_VERSION),
          ActorRef.noSender());
    }
  }

  /**
   * Simulation metadata.
   * <p>
   */
  private SimulationMetadata simulationMetadata;

  /**
   * Reporter actor reference is the reference to the actor which receives reporting events.
   * <p>
   */
  private ActorRef loggerActor;

  /**
   * Reporter actor reference to write log events to the Influx DB.
   * <p>
   */
  private ActorRef influxActor;

  /**
   * StdOut reporter is to write out about the test execution to the stdout. It can be considered as
   * heartbeat about the running test.
   * <p>
   */
  private ActorRef stdOutReptorter;

  private ActorSystem system = ActorSystem.create(ACTOR_SYS_NAME);

  public static EventDispatcher getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new EventDispatcher(Simulation.getData().orElseThrow());
    }

    return INSTANCE;
  }

  public void dispatchEvents(MeasurementImpl measurement) {
    try {
      measurement.getEvents().forEach(e -> {
        loggerActor.tell(e, ActorRef.noSender());
        stdOutReptorter.tell(e, ActorRef.noSender());

        if (simulationMetadata.isEnableInflux()) {
          influxActor.tell(e, ActorRef.noSender());
        }
      });
    } finally {
      measurement.purge();
    }
  }

  public void stop() {

    requestForTermination();

    var terminate = system.terminate();

    terminate.onComplete(new OnComplete<>() {

      @Override
      public void onComplete(final Throwable throwable, final Terminated terminated) {
        system = null;
      }

    }, system.dispatcher());
  }

  private void requestForTermination() {
    var ask = Patterns.ask(stdOutReptorter, new EndTestEvent(Instant.now()),
        TERMINATION_REQUEST_TIMEOUT);
    try {
      Await.result(ask, FiniteDuration.Inf());
    } catch (Exception e) {
      LOG.debug(e); // expected exception is a Timeout. It is ok.
    }
  }
}
