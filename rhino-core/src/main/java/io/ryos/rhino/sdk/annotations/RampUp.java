package io.ryos.rhino.sdk.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RampUp {

  long startRps() default 0;

  long targetRps() default 0;

  int duration() default 1;
}
