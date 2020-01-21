package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.dsl.data.builder.MapperBuilder;

public interface MappableDsl extends DslItem {

  <R, T> LoadDsl map(MapperBuilder<R, T> mapper);
}
