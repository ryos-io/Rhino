package io.ryos.rhino.sdk.dsl;

/**
 * DSL Starter.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class Start {

  private Start() {
  }

  public static RunnableDsl dsl() {
    return new RunnableDslImpl();
  }
}
