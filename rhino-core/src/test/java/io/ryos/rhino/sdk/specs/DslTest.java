package io.ryos.rhino.sdk.specs;

import static io.ryos.rhino.sdk.specs.Spec.http;
import static org.asynchttpclient.Dsl.get;

import io.ryos.rhino.sdk.dsl.SpecMaterializer;
import io.ryos.rhino.sdk.dsl.Start;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DslTest {

  public static Function<Response, String> session(String content) {
    return (Response r) -> {
      return content;
    };
  }

  @Test
  public void testExecSpec() throws InterruptedException {

  }

  @Test
  public void testDsl() {

    Start
        .spec()
        .run(http("first").endpoint((r)-> "http://bagdemir.com" ).get() )
        .run(http("first").endpoint((r)-> "http://bagdemir.com" ).get() )
        ;
  }

  @Test
  public void testMonoChain() {
    Flux.fromStream(IntStream.range(0, 10).boxed()).flatMap(s -> Mono.fromSupplier(() -> s * 2))
        .subscribe(System.out::println);
  }
}
