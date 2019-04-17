package com.adobe.rhino.sdk.annotations;

import com.adobe.rhino.sdk.feeders.Feed;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark the {@link Feeder} injection point. Feeders are data providers.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @see Feeder
 * @since 1.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Feeder {

  /**
   * Factory implementation for {@link Feeder} instances.
   *
   * @return Factory implementation.
   */
  Class<? extends Feed> factory();
}
