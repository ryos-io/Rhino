package io.ryos.rhino.sdk.dsl;

/**
 * Use DSL starter instance in DSL methods to start with a DSL.
 *
 * @author Erhan Bagdemir
 * @since 1.0.0
 */
public class Start {

  public static ThreadLocal<String> dslMethodName = new ThreadLocal<>();

  private Start() {
  }

  public static RunnableDsl dsl() {
    return new LoadDslImpl(dslMethodName.get());
  }
}
