package io.ryos.rhino.sdk.specs;

import io.netty.handler.codec.http.HttpHeaders;
import io.ryos.rhino.sdk.SimulationMetadata;
import io.ryos.rhino.sdk.reporting.MeasurementImpl;
import io.ryos.rhino.sdk.reporting.UserEvent;
import io.ryos.rhino.sdk.reporting.UserEvent.EventType;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.Response;
import org.asynchttpclient.netty.request.NettyRequest;

public class HttpSpecAsyncHandler implements AsyncHandler<Response> {

  private final String stepName;
  private final String specName;
  private final int userId;
  private final MeasurementImpl measurement;
  private final SimulationMetadata simulationMetadata;
  private volatile long start = -1;
  private volatile int status;

  public HttpSpecAsyncHandler(int userId, String specName, String stepName, SimulationMetadata simulationMetadata) {
    this.measurement = new MeasurementImpl(specName, userId);
    this.specName = specName;
    this.userId = userId;
    this.simulationMetadata = simulationMetadata;
    this.stepName = stepName;
  }

  @Override
  public State onStatusReceived(final HttpResponseStatus responseStatus) {

    this.status = responseStatus.getStatusCode();

    return State.CONTINUE;
  }

  @Override
  public State onHeadersReceived(final HttpHeaders headers) {
    return State.CONTINUE;
  }

  @Override
  public State onBodyPartReceived(final HttpResponseBodyPart bodyPart) {
    return State.CONTINUE;
  }

  @Override
  public void onThrowable(final Throwable t) {

  }

  @Override
  public Response onCompleted() {
    var elapsed = System.currentTimeMillis() - start;

    measurement.measure(stepName, String.valueOf(status));

    var userEventEnd = new UserEvent();
    userEventEnd.elapsed = elapsed;
    userEventEnd.start = start;
    userEventEnd.end = start + elapsed;
    userEventEnd.scenario = specName;
    userEventEnd.eventType = EventType.END;
    userEventEnd.id = userId;
    measurement.record(userEventEnd);

    EventDispatcher.instance(simulationMetadata).dispatchEvents(measurement);
    return null;
  }

  @Override
  public void onRequestSend(NettyRequest request) {

    this.start = System.currentTimeMillis();
    var userEventStart = new UserEvent();
    userEventStart.elapsed = 0;
    userEventStart.start = start;
    userEventStart.end = start;
    userEventStart.scenario = specName;
    userEventStart.eventType = EventType.START;
    userEventStart.id = userId;
    measurement.record(userEventStart);
  }
}
