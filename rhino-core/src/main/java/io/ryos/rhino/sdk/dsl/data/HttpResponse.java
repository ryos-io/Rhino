package io.ryos.rhino.sdk.dsl.data;

import org.asynchttpclient.Response;

public class HttpResponse {

  private Response response;

  public HttpResponse(final Response response) {
    this.response = response;
  }

  public Response getResponse() {
    return response;
  }

  public String getResponseBodyAsString() {
    return response.getResponseBody();
  }


  public int getStatusCode() {
    return response.getStatusCode();
  }
}
