/*
 * Copyright 2018 Ryos.io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ryos.rhino.sdk.dsl.specs.impl;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.mat.MapperSpecMaterializer;
import io.ryos.rhino.sdk.dsl.mat.SpecMaterializer;
import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.MapperSpec;
import io.ryos.rhino.sdk.dsl.specs.builder.MapperBuilder;

/**
 * Mapper spec implementation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public class MapperSpecImpl<R, T> extends AbstractMeasurableSpec implements MapperSpec {

  private final MapperBuilder<R, T> mapper;

  public MapperSpecImpl(MapperBuilder<R, T> mapper) {

    super("N/A");

    this.mapper = mapper;
  }

  public MapperBuilder<R, T> getMapper() {
    return mapper;
  }

  @Override
  public SpecMaterializer<? extends DSLSpec> createMaterializer(UserSession session) {
    return new MapperSpecMaterializer();
  }
}
