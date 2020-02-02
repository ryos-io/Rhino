package io.ryos.rhino.sdk.dsl;

public interface MeasureDsl extends MaterializableDslItem {

  LoadDsl measure(final String tag, final MaterializableDslItem dslItem);
}
