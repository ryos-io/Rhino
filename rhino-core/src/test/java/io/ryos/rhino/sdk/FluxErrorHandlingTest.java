package io.ryos.rhino.sdk;

import java.util.stream.IntStream;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Ignore
public class FluxErrorHandlingTest {

  @Test
  public void testErrorHandling() {

    Flux.fromStream(IntStream.range(0, 10).boxed())
        .flatMap((i) -> {
          System.out.println(i);
          if (i % 2 == 0) {
            throw new RuntimeException();
          }
          return Mono.just(i);
        })
        .onErrorContinue((t) -> t instanceof IllegalArgumentException, (a, b) -> {
          System.out.println(b);
        })
        .blockLast();
  }
}
