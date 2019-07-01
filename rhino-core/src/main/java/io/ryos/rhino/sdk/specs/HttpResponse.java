package io.ryos.rhino.sdk.specs;

import org.asynchttpclient.Response;

public class HttpResponse {

  private Response response;

  public HttpResponse(final Response response) {
    this.response = response;
  }

  public Response getResponse() {
    return response;
  }

  public int getStatusCode() {
    return response.getStatusCode();
  }
}
