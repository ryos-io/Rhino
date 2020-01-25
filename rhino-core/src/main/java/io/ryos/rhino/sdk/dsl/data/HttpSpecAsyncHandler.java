package io.ryos.rhino.sdk.dsl.data;

import io.netty.handler.codec.http.HttpHeaders;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.DslMethod;
import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.dsl.impl.AbstractMeasurableDsl;
import io.ryos.rhino.sdk.dsl.impl.HttpDslImpl.RetryInfo;
import io.ryos.rhino.sdk.reporting.MeasurementImpl;
import io.ryos.rhino.sdk.reporting.UserEvent;
import io.ryos.rhino.sdk.reporting.UserEvent.EventType;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.Response;
import org.asynchttpclient.netty.request.NettyRequest;

public class HttpSpecAsyncHandler implements AsyncHandler<Response> {

  public static final Logger LOG = LogManager.getLogger(HttpSpecAsyncHandler.class);
  private static final String BLANK = "";

  private final String stepName;
  private final String dslName;
  private final String userId;
  private final boolean measurementEnabled;
  private final boolean cumulativeMeasurement;
  private final MeasurementImpl measurement;
  private volatile long start = -1;
  private volatile int status;
  private final Response.ResponseBuilder builder = new Response.ResponseBuilder();
  private final EventDispatcher eventDispatcher;
  private final RetryInfo retryInfo;

  public HttpSpecAsyncHandler(final UserSession session,
      final HttpDsl dslItem) {
    this.measurement = new MeasurementImpl(getMeasurementName(dslItem), session.getUser().getId());
    this.dslName = dslItem.getParentName();
    this.userId = session.getUser().getId();
    this.stepName = dslItem.getMeasurementPoint();
    this.eventDispatcher = EventDispatcher.getInstance();
    this.measurementEnabled = dslItem.isMeasurementEnabled();
    this.retryInfo = dslItem.getRetryInfo();
    this.cumulativeMeasurement = dslItem.isCumulative();
  }

  private String getMeasurementName(final HttpDsl dslItem) {
    if (dslItem.hasParent()) {
      var parent = dslItem.getParent();
      if (parent instanceof AbstractMeasurableDsl) {
        return ((AbstractMeasurableDsl) parent).getMeasurementPoint();
      }
      if (parent instanceof DslMethod) {
        return parent.getName();
      }
    }
    return dslItem.getName();
  }

  @Override
  public State onStatusReceived(final HttpResponseStatus responseStatus) {
    builder.reset();
    builder.accumulate(responseStatus);
    status = responseStatus.getStatusCode();

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
      var userEventStart = new UserEvent(
          BLANK,
          userId,
          dslName,
          start,
          start,
          0L,
          EventType.START,
          BLANK,
          userId
      );

      measurement.record(userEventStart);
    }

    // Store the error event in the measurement stack.
    measurement.measure(t.getMessage(), "N/A");

    var userEventEnd = new UserEvent(
        BLANK,
        userId,
        dslName,
        start,
        0,
        0L,
        EventType.END,
        BLANK,
        userId
    );

    measurement.record(userEventEnd);

    eventDispatcher.dispatchEvents(measurement);
  }

  @Override
  public Response onCompleted() {
    var response = builder.build();
    var httpResponse = new HttpResponse(response);
    if (measurementEnabled && isReadyToMeasure(httpResponse)) {
      completeMeasurement();
    }
    if (SimulationConfig.debugHttp()) {
      LOG.info("[debug.http=true][statusCode={}][body={}]",
          response.getStatusCode(),
          response.getResponseBody());
    }
    return response;
  }

  public void completeMeasurement() {

    var elapsed = System.currentTimeMillis() - start;

    measurement.measure(stepName, String.valueOf(status));
    var userEventEnd = new UserEvent(
        BLANK,
        userId,
        dslName,
        start,
        start + elapsed,
        elapsed,
        EventType.END,
        BLANK,
        userId
    );

    measurement.record(userEventEnd);

    eventDispatcher.dispatchEvents(measurement);
  }

  private boolean isReadyToMeasure(HttpResponse httpResponse) {
    if (retryInfo == null) {
      return true;
    }

    if (!cumulativeMeasurement) {
      return true;
    }

    return !retryInfo.getPredicate().test(httpResponse);
  }

  @Override
  public void onRequestSend(NettyRequest request) {

    if (measurementEnabled) {

      // if the start timestamp is not set, then set it. Otherwise, if it is a cumulative
      // measurement, and the start is already set, then skip it.
      if (start < 0 || !cumulativeMeasurement) {
        this.start = System.currentTimeMillis();
      }

      var userEventStart = new UserEvent(
          "",
          userId,
          dslName,
          start,
          start,
          0L,
          EventType.START,
          BLANK,
          userId
      );

      measurement.record(userEventStart);
    }
  }
}
