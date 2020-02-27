package io.ryos.rhino.sdk.dsl.impl;

import io.ryos.rhino.sdk.dsl.GaugeDsl;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.mat.DslMaterializer;
import io.ryos.rhino.sdk.dsl.mat.MeasureDslMaterializer;
import java.util.Collections;
import java.util.List;

public class GaugeDslImpl extends AbstractDSLItem implements GaugeDsl {

  private final String tag;
  private final MaterializableDslItem childDsl;

  public GaugeDslImpl(final String tag, final MaterializableDslItem childDsl) {
    super(tag);

    this.tag = tag;
    this.childDsl = childDsl;
  }

  @Override
  public List<MaterializableDslItem> getChildren() {
    return Collections.singletonList(childDsl);
  }

  @Override
  public MaterializableDslItem getMeasureableItem() {
    return childDsl;
  }

  @Override
  public DslMaterializer materializer() {
    return new MeasureDslMaterializer(this);
  }

  @Override
  public String getTag() {
    return tag;
  }
}
