package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.mat.SpecMaterializer;

public interface Materializable {

  /**
   * Create materializer instance for this spec instance.
   * <p>
   *
   * @param userSession User session of current load cycle.
   * @param <T> Result object type.
   * @return {@link SpecMaterializer} instance.
   */
  <T extends DSLSpec> SpecMaterializer<T> createMaterializer(UserSession userSession);
}
