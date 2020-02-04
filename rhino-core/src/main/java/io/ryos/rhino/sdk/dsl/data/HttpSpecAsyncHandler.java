package io.ryos.rhino.sdk.dsl.data;

import io.netty.handler.codec.http.HttpHeaders;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.dsl.impl.HttpDslImpl.RetryInfo;
import io.ryos.rhino.sdk.reporting.MeasurementImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.Response;
import org.asynchttpclient.netty.request.NettyRequest;

public class HttpSpecAsyncHandler implements AsyncHandler<Response> {

  public static final Logger LOG = LogManager.getLogger(HttpSpecAsyncHandler.class);
  private final MeasurementImpl measurement;
  private volatile long start = -1;
  private volatile int status;
  private final Response.ResponseBuilder builder = new Response.ResponseBuilder();
  private final RetryInfo retryInfo;

  public HttpSpecAsyncHandler(final UserSession session, final HttpDsl dslItem) {
    this.measurement = new MeasurementImpl(session.getUser().getId(), dslItem);
    this.retryInfo = dslItem.getRetryInfo();
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
    measurement.fail(t.getMessage());
  }

  @Override
  public Response onCompleted() {
    var response = builder.build();
    var httpResponse = new HttpResponse(response);
    if (measurement.isMeasurementEnabled() && isReadyToMeasure(httpResponse)) {
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
    measurement.measure(String.valueOf(status));
    measurement.finish();
  }

  private boolean isReadyToMeasure(HttpResponse httpResponse) {
    if (retryInfo == null) {
      return true;
    }

    if (!measurement.isCumulativeMeasurement()) {
      return true;
    }

    return !retryInfo.getPredicate().test(httpResponse);
  }

  @Override
  public void onRequestSend(NettyRequest request) {
    measurement.start();
  }
}
