package io.ryos.rhino.sdk.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Ramp-up annotation is used to increase the load in controlled steps, from start rps till it
 * reaches the target RPS during the ramp-up phase defined with duration.
 *
 * If you wish you can override this annotation's properties als via System properties,e.g.
 * <code>-Dsimulation.rampup.io.ryos.rhino.test.ReactiveSleepTestSimulation.startRps=100</code>
 * <p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RampUp {

  /**
   * Start request-per-second.
   * <p>
   *
   * @return Start RPS.
   */
  long startRps() default 0;

  /**
   * Target request-per-second.
   * <p>
   *
   * @return Target request-per-second.
   */
  long targetRps() default 100;

  /**
   * Ramp-up duration in minutes. After the duration elapses, the request stays at the level of
   * target RPS.
   * <p>
   *
   * @return Ramp-up phase.
   */
  int durationInMins() default -1;
}
