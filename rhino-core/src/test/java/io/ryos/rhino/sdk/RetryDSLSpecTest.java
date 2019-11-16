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

package io.ryos.rhino.sdk;

import io.ryos.rhino.sdk.exceptions.RetryableOperationException;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.RequestBuilder;
import org.junit.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RetryDSLSpecTest {

  @Test
  public void testRetryHttpRequest() {
    RequestBuilder requestBuilder = Dsl.get("http://bagdemir.com/a");
    var httpClientConfig = Dsl.config()
        .setConnectTimeout(1000)
        .setMaxConnections(1000)
        .setKeepAlive(true)
        .build();

    var client = Dsl.asyncHttpClient(httpClientConfig);

    Mono.fromFuture(client.executeRequest(requestBuilder).toCompletableFuture())
        .map(r -> {
          if (r.getStatusCode() == 404) {
            throw new RuntimeException();
          }

          return r;
        })
        .retryWhen(companion -> companion
            .zipWith(Flux.range(1, 4),
                (error, index) -> {
                  if (index < 4 && error instanceof RetryableOperationException) {
                    return index;
                  } else {
                    throw Exceptions.propagate(error);
                  }
                }))
        .doOnNext(r -> System.out.println(r))
        .doOnError(t -> System.out.println(t.getMessage()))
        .subscribe();
  }
}
