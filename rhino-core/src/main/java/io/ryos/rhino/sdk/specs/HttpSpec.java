package io.ryos.rhino.sdk.specs;

public interface HttpSpec extends Spec {

  HttpSpec get();
  HttpSpec head();
  HttpSpec put();
  HttpSpec post();
  HttpSpec delete();
  HttpSpec patch();
  HttpSpec options();

  HttpSpec target(String endpoint);

  HttpSpec headers(String name, String ... values);
  HttpSpec queryParam(String name, String ... values);
  HttpSpec matrixParams(String name, String value);
}
