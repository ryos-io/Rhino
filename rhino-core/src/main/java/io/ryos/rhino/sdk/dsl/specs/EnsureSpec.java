package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.data.UserSession;
import java.util.function.Predicate;

/**
 * Ensure spec is used to terminate a simulation if the predicate is not fulfilled.
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface EnsureSpec extends Spec {

  Predicate<UserSession> getPredicate();

  String getCause();
}
