package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.mat.DslMaterializer;

public interface MaterializableDsl {

  /**
   * Create materializer instance for this spec instance.
   * <p>
   *
   * @param userSession User session of current load cycle.
   * @param <T> Result object type.
   * @return {@link DslMaterializer} instance.
   */
  <T extends MaterializableDslItem> DslMaterializer<T> materializer(UserSession userSession);
}
