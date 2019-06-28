package io.ryos.rhino.sdk.specs;

import java.util.function.Predicate;

public interface RetriableSpec<R extends Spec, T> extends Spec {

  R retryIf(Predicate<T> predicate, int numOfRetries);
}
