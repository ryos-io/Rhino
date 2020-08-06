/*
 * Copyright 2018 Ryos.io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ryos.rhino.sdk.dsl.mat;

import io.ryos.rhino.sdk.dsl.data.HttpResponse;
import java.io.InputStream;
import java.util.List;

public class HttpDslData {

  private String endpoint;
  private HttpResponse response;

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public HttpResponse getResponse() {
    return response;
  }

  public List<String> getHeaderValues(String headerName) {
    return getResponse().getResponse().getHeaders(headerName);
  }

  public String getHeaderValue(String headerName) {
    return getResponse().getResponse().getHeader(headerName);
  }

  public int getStatusCode() {
    return response.getStatusCode();
  }

  public String getResponseBodyAsString() {
    return response.getResponse().getResponseBody();
  }

  public InputStream getResponseBodyAsStream() {
    return response.getResponse().getResponseBodyAsStream();
  }

  public void setResponse(HttpResponse response) {
    this.response = response;
  }
}
