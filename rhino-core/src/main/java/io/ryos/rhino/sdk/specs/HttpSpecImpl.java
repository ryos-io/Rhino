package io.ryos.rhino.sdk.specs;

import io.ryos.rhino.sdk.SimulationConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.resources.ConnectionProvider;

public class HttpSpecImpl implements HttpSpec {

  private String target;
  private Map<String, List<String>> queryParams = new HashMap<>();
  private Map<String, List<String>> headers = new HashMap<>();
  private Map<String, String> matrixParams = new HashMap<>();
  private HttpClient client = HttpClient.create(ConnectionProvider.fixed("rhino",
      SimulationConfig.getMaxConnections()));
  private Mono<HttpClientResponse> responseMono;

  @Override
  public HttpSpec get() {

    this.responseMono = client
        .baseUrl(target)
        .get()
        .response();

    return this;
  }

  @Override
  public HttpSpec head() {
    this.responseMono = client
        .baseUrl(target)
        .head()
        .response();

    return this;
  }

  @Override
  public HttpSpec put() {
    throw new NotImplementedException("Http PUT()");
  }

  @Override
  public HttpSpec post() {
    throw new NotImplementedException("Http POST()");
  }

  @Override
  public HttpSpec delete() {
    throw new NotImplementedException("Http DELETE()");
  }

  @Override
  public HttpSpec patch() {
    throw new NotImplementedException("Http PATCH()");
  }

  @Override
  public HttpSpec options() {
    throw new NotImplementedException("Http OPTIONS()");
  }

  @Override
  public HttpSpec target(final String endpoint) {
    this.target = endpoint;
    return this;
  }

  @Override
  public HttpSpec headers(final String name, final String... values) {
    this.headers.put(name, Arrays.asList(values));
    return this;
  }

  @Override
  public HttpSpec queryParam(final String name, final String... values) {
    this.queryParams.put(name, Arrays.asList(values));
    return this;
  }

  @Override
  public HttpSpec matrixParams(final String name, final String value) {
    this.matrixParams.put(name, value);
    return this;
  }

  @Override
  public Mono<HttpClientResponse> toMono() {
    return responseMono; // does nothing
  }
}
