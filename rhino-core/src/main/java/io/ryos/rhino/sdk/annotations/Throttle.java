package io.ryos.rhino.sdk.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation is used to limit the number of requests that the framework is allowed to produce
 * within the duration.
 * <p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Throttle {

  /**
   * Duration in minutes in which the throttling is active.
   * <p>
   *
   * @return Throttling duration.
   */
  int durationInMins() default 1;

  /**
   * Max. number of requests.
   *
   * @return Maximum number of requests.
   */
  int numberOfRequests() default 5;
}
