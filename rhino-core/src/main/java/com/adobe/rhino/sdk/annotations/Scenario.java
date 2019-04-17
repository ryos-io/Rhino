package com.adobe.rhino.sdk.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Scenario annotation is to mark the benchmark testing steps. The methods annotated with this
 * annotation will be run during benchmark within a step.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Scenario {

  /**
   * Returns the name of the scenario.
   *
   * @return The name of the scenario.
   */
  String name();
}
