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
  private volatile int status;

  private final MeasurementImpl measurement;
  private final Response.ResponseBuilder builder = new Response.ResponseBuilder();
  private final RetryInfo retryInfo;
  private final UserSession session;

  public HttpSpecAsyncHandler(final UserSession session, final HttpDsl dslItem) {
    this.session = session;
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
    if (isReadyToMeasure(httpResponse)) {
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
    long measure = measurement.measure(String.valueOf(status));

    session.notify(measure);

    measurement.finish();
  }

  private boolean isReadyToMeasure(HttpResponse httpResponse) {
    // if the request needs to be retried and the measure is not cumulative measurement, i.e
    // every retry will be measured.
    if (retryInfo == null || !measurement.isCumulativeMeasurement()) {
      return true;
    }

    return !retryInfo.getPredicate().test(httpResponse);
  }

  @Override
  public void onRequestSend(NettyRequest request) {
    measurement.start();
  }
}
