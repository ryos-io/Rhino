package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.mat.SpecMaterializer;

public interface MaterializableDsl {

  /**
   * Create materializer instance for this spec instance.
   * <p>
   *
   * @param userSession User session of current load cycle.
   * @param <T> Result object type.
   * @return {@link SpecMaterializer} instance.
   */
  <T extends MaterializableDslItem> SpecMaterializer<T> materializer(UserSession userSession);
}
