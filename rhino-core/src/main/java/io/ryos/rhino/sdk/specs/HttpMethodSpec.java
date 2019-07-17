package io.ryos.rhino.sdk.specs;

/**
 * Http method specification consists of methods of Http verbs, e.g get, head, post, ...
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface HttpMethodSpec extends Spec {

  HttpRetriableSpec get();
  HttpRetriableSpec head();
  HttpRetriableSpec put();
  HttpRetriableSpec post();
  HttpRetriableSpec delete();
  HttpRetriableSpec patch();
  HttpRetriableSpec options();
}
