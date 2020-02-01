package io.ryos.rhino.sdk.dsl.impl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.DslItem;
import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.mat.DslMaterializer;
import java.util.ArrayList;
import java.util.List;

public class MeasureDslImpl extends AbstractDSLItem implements MaterializableDslItem {

  private final String tag;
  private final MaterializableDslItem dslItem;

  public MeasureDslImpl(final String tag, final MaterializableDslItem dslItem) {
    super(tag);

    this.tag = tag;
    this.dslItem = dslItem;
  }

  public String getTag() {
    return tag;
  }

  public DslItem getDslItem() {
    return dslItem;
  }

  @Override
  public List<MaterializableDslItem> getChildren() {
    final ArrayList<MaterializableDslItem> objects = new ArrayList<>();
    objects.add(dslItem);
    return objects;
  }

  @Override
  public <T extends MaterializableDslItem> DslMaterializer<T> materializer(
      final UserSession userSession) {

    return null;
  }
}
