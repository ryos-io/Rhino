package io.ryos.rhino.sdk.specs;

import java.util.List;
import java.util.Map;

public interface HttpSpec extends Spec {

  enum Method {GET, HEAD, PUT, POST}

  HttpSpec get();
  HttpSpec head();
  HttpSpec put();
  HttpSpec post();
  HttpSpec delete();
  HttpSpec patch();
  HttpSpec options();


  HttpSpec target(String endpoint);

  HttpSpec headers(String name, String ... values);
  HttpSpec queryParam(String name, String value);
  HttpSpec matrixParams(String name, String value);

  // Getters
  Method getMethod();
  String getTarget();

  Map<String, List<Object>> getHeaders();
  Map<String, String> getQueryParameters();

  String getEnclosingSpec();
  String getStepName();

}
