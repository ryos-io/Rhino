package io.ryos.rhino.sdk;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.filter.ThrottleRequestFilter;

/**
 * HttpClient singleton instance.
 *
 * @author Erhan Bagdemir
 * @since 2.0.0
 */
public enum HttpClient {

  INSTANCE;

  private AsyncHttpClient client;

  HttpClient() {

    var httpClientConfig = Dsl.config()
        .setKeepAlive(true)
        .setMaxConnections(SimulationConfig.getMaxConnections())
        .setMaxConnectionsPerHost(SimulationConfig.getMaxConnections())
        .setConnectTimeout(SimulationConfig.getHttpConnectTimeout())
        .setHandshakeTimeout(SimulationConfig.getHttpHandshakeTimeout())
        .setReadTimeout(SimulationConfig.getHttpReadTimeout())
        .setRequestTimeout(SimulationConfig.getHttpRequestTimeout())
        .addRequestFilter(new ThrottleRequestFilter(SimulationConfig.getMaxConnections()))
        .build();

    this.client = Dsl.asyncHttpClient(httpClientConfig);
  }

  public AsyncHttpClient getClient() {
    return client;
  }
}
