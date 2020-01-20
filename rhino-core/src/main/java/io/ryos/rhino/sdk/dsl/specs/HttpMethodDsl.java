package io.ryos.rhino.sdk.dsl.specs;

/**
 * Http method specification consists of methods of Http verbs, e.g get, head, post, ...
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface HttpMethodDsl extends MaterializableDslItem {

  HttpRetriableDsl get();
  HttpRetriableDsl head();
  HttpRetriableDsl put();
  HttpRetriableDsl post();
  HttpRetriableDsl delete();
  HttpRetriableDsl patch();
  HttpRetriableDsl options();
}
