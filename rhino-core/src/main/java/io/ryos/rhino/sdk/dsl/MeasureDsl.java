package io.ryos.rhino.sdk.dsl;

public interface MeasureDsl extends MaterializableDslItem {

  DslBuilder measure(final String tag, final MaterializableDslItem dslItem);
}
