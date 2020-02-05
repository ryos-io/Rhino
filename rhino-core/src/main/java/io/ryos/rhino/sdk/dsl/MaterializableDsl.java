package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.mat.DslMaterializer;

/**
 * A materializable DSLs are those which have their reactive component counterparts. They are
 * materialized into reactive components and become part of reactive pipeline.
 *
 * @author Erhan Bagdemir
 */
public interface MaterializableDsl {

  /**
   * Create materializer instance for this spec instance.
   * <p>
   *
   * @param <T> Result object type.
   * @return {@link DslMaterializer} instance.
   */
  <T extends MaterializableDslItem> DslMaterializer materializer();
}
