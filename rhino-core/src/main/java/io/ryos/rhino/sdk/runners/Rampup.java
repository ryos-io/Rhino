/*************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2019 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 **************************************************************************/
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

  public static <T> Function<Flux<T>, Flux<T>> rampup(final long startRps, final long endRps,
      final Duration duration) {
    Preconditions.checkArgument(startRps > 0, "startRps <= 0");
    Preconditions.checkArgument(endRps > 1, "endRps < 1");
    Objects.requireNonNull(duration, "duration is null");

    final double slope = Math.abs(((double) (endRps - startRps)) / duration.toSeconds());
    final long startTime = System.currentTimeMillis();
    Flux<Long> rampup = Flux.<Long>generate(sink -> sink.next(0L)).delayUntil(d -> {
      // TODO get the start time from the context?
      long t = (System.currentTimeMillis() - startTime) / 1000;
      long rps = 0;
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
