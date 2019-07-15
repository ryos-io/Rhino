package io.ryos.rhino.sdk.dsl;

public class Start {

  private Start() {
  }

  public static ConnectableDsl dsl() {
    return new ConnectableDsl();
  }
}
