package io.ryos.rhino.sdk.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Ramp-up annotation is used to increase the load in controlled steps, from start rps till it
 * reaches the target RPS during the ramp-up phase defined with duration.
 * <br>
 * If you wish you can override this annotation's properties als via System properties,e.g.
 * <code>-Dsimulation.rampup.io.ryos.rhino.test.ReactiveSleepTestSimulation.startRps=100</code>
 * <p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RampUp {

  /**
   * Start request-per-second.
   * <br>
   * If you wish you can override this annotation's properties als via System properties,e.g.
   * <code>-Dsimulation.rampup.io.ryos.rhino.test.ReactiveSleepTestSimulation.startRps=10</code>
   * <p>
   *
   * @return Start RPS.
   */
  long startRps() default 1;

  /**
   * Target request-per-second.
   * <br>
   * If you wish you can override this annotation's properties als via System properties,e.g.
   * <code>-Dsimulation.rampup.io.ryos.rhino.test.ReactiveSleepTestSimulation.targetRps=100</code>
   * <p>
   *
   * @return Target request-per-second.
   */
  long targetRps() default 100;

  /**
   * Ramp-up duration in minutes. After the duration elapses, the request stays at the level of
   * target RPS.
   * <br>
   * If you wish you can override this annotation's properties als via System properties,e.g.
   * <code>-Dsimulation.rampup.io.ryos.rhino.test.ReactiveSleepTestSimulation.durationInMins=60</code>
   * <p>
   *
   * @return Ramp-up phase.
   */
  int durationInMins() default -1;
}
