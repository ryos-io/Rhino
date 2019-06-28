package io.ryos.rhino.sdk.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Throttle {

  int durationInMins() default 1;
  int numberOfRequests() default 5;
}