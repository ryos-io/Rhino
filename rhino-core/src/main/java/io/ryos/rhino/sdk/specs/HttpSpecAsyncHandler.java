package io.ryos.rhino.sdk.specs;

import io.netty.handler.codec.http.HttpHeaders;
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
  private final String userId;
  private final Boolean measurementEnabled;
  private final MeasurementImpl measurement;
  private volatile long start = -1;
  private volatile int status;
  private final Response.ResponseBuilder builder = new Response.ResponseBuilder();
  private final EventDispatcher eventDispatcher;

  public HttpSpecAsyncHandler(final String userId,
      final String specName,
      final String stepName,
      final EventDispatcher eventDispatcher,
      final Boolean measurementEnabled) {
    this.measurement = new MeasurementImpl(specName, userId);
    this.specName = specName;
    this.userId = userId;
    this.stepName = stepName;
    this.eventDispatcher = eventDispatcher;
    this.measurementEnabled = measurementEnabled;
  }

  @Override
  public State onStatusReceived(final HttpResponseStatus responseStatus) {

    builder.reset();
    builder.accumulate(responseStatus);
    this.status = responseStatus.getStatusCode();

    return State.CONTINUE;
  }

  @Override
  public State onHeadersReceived(final HttpHeaders headers) {
    builder.accumulate(headers);
    return State.CONTINUE;
  }

  @Override
  public State onBodyPartReceived(final HttpResponseBodyPart bodyPart) {
    builder.accumulate(bodyPart);
    return State.CONTINUE;
  }

  /**
   * Handling the errors came out of the Http client, e.g connection timeouts.
   * <p>
   *
   * @param t Throwable instance.
   */
  @Override
  public void onThrowable(final Throwable t) {
    // There is no start event for user measurement, so we need to create one.
    // In Error case, we just want to make the error visible in stdout. We don't actually record
    // any metric here, thus the start/end timestamps are irrelevant.
    if (!measurementEnabled) {
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

    // Store the error event in the measurement stack.
    measurement.measure(t.getMessage(), "N/A");

    var userEventEnd = new UserEvent();
    userEventEnd.elapsed = 0;
    userEventEnd.start = start;
    userEventEnd.end = 0;
    userEventEnd.scenario = specName;
    userEventEnd.eventType = EventType.END;
    userEventEnd.id = userId;
    measurement.record(userEventEnd);

    eventDispatcher.dispatchEvents(measurement);
  }

  @Override
  public Response onCompleted() {

    if (measurementEnabled) {
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

      eventDispatcher.dispatchEvents(measurement);
    }

    return builder.build();
  }

  @Override
  public void onRequestSend(NettyRequest request) {

    if (measurementEnabled) {
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
}
