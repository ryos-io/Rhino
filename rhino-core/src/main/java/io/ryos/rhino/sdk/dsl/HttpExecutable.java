package io.ryos.rhino.sdk.dsl;

public class HttpExecutable implements Executable {

  /**
   * Name of the executable. The name will be used in reporting together with enclosing spec name.
   * <p>
   */
  private String name;

  /**
   * The name of the spec, that is set on annotation. The spec name will be used in reporting
   * together with name of the executable.
   * <p>
   */
  private String enclosingTypeName;

  public HttpExecutable(String name) {
    this.name = name;
  }

  @Override
  public void execute() {

  }
}
