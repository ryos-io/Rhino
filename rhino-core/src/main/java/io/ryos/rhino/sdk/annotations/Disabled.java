package io.ryos.rhino.sdk.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Disables the scenario or DSL. Disabled DSLs/Scenarios will not be run by runners.
 *
 * @author Erhan Bagdemir
 * @since 1.8.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Disabled {
}
