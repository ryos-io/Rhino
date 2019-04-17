package com.adobe.rhino.sdk.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marker annotation to activate Influx DB writer.
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @since 1.1.4
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Influx {
}
