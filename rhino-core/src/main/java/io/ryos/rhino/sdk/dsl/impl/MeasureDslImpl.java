package io.ryos.rhino.sdk.dsl.impl;

import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.mat.DslMaterializer;
import io.ryos.rhino.sdk.dsl.mat.MeasureDslMaterializer;
import java.util.Collections;
import java.util.List;

public class MeasureDslImpl extends AbstractDSLItem implements MaterializableDslItem {

  private final String tag;
  private final MaterializableDslItem childDsl;

  public MeasureDslImpl(final String tag, final MaterializableDslItem childDsl) {
    super(tag);

    this.tag = tag;
    this.childDsl = childDsl;
  }

  @Override
  public List<MaterializableDslItem> getChildren() {
    return Collections.singletonList(childDsl);
  }

  @Override
  public DslMaterializer materializer() {
    return new MeasureDslMaterializer(this);
  }

  public String getTag() {
    return tag;
  }
}
