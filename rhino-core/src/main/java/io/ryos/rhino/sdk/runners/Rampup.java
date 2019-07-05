package io.ryos.rhino.sdk.runners;

import static java.time.Duration.ofNanos;

import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class Rampup {
  private static final Logger LOG = LoggerFactory.getLogger(Rampup.class);

  /**
   * Defines a linear ramp up of the load beginning with the start rate until target rate is reached.
   * Rates are defined in requests per second.
   * @param startRps start rate in request per second
   * @param targetRps end rate in requests per second
   * @param duration time given to reach target rate
   * @return Function for a Flux transformation
   */
  public static <T> Function<Flux<T>, Flux<T>> rampup(final long startRps, final long targetRps,
      final Duration duration) {
    Preconditions.checkArgument(startRps >= 0, "startRps < 0");
    Preconditions.checkArgument(targetRps > 1, "targetRps < 1");
    Objects.requireNonNull(duration, "duration is null");

    final double slope = Math.abs(((double) (targetRps - startRps)) / duration.toSeconds());
    final long startTime = System.currentTimeMillis();
    Flux<Long> rampup = Flux.<Long>generate(sink -> sink.next(0L)).delayUntil(d -> {
      // TODO get the start time from the context?
      long t = (System.currentTimeMillis() - startTime) / 1000;
      long rps = targetRps;
      if (t <= duration.toSeconds()) {
        rps = (long) Math.floor(startRps + slope * t);
      }
      long tickNano = (long) Math.floor((1d / rps) * 1e9);
      LOG.debug("tickNano={}, rps={}, t={}", tickNano, rps, t);
      return Mono.delay(ofNanos(tickNano));
    });

    final UnaryOperator<Flux<T>> res = f ->
        f.zipWith(rampup.concatWith(Flux.generate(sink -> sink.next(0L))))
            .map(Tuple2::getT1);
    return res;
  }
}
