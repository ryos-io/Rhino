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

package io.ryos.rhino.sdk.dsl.impl;

import io.ryos.rhino.sdk.dsl.MaterializableDslItem;
import io.ryos.rhino.sdk.dsl.WaitDsl;
import io.ryos.rhino.sdk.dsl.mat.DslMaterializer;
import io.ryos.rhino.sdk.dsl.mat.WaitDslMaterializer;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/**
 * Wait spec implementation that halts the execution for {@link Duration} given.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class WaitDslImpl extends AbstractMeasurableDsl implements WaitDsl {

  private static final String BLANK = "";
  private final Duration waitTime;

  public WaitDslImpl(final Duration duration) {
    this(BLANK, Validate.notNull(duration, "Duration must not be null."));
  }

  public WaitDslImpl(final String name, final Duration duration) {
    super(Objects.requireNonNull(name));

    this.waitTime = Validate.notNull(duration, "Duration must not be null.");
  }

  @Override
  public Duration getWaitTime() {
    return waitTime;
  }

  @Override
  public DslMaterializer materializer() {
    return new WaitDslMaterializer(this);
  }

  @Override
  public List<MaterializableDslItem> getChildren() {
    return Collections.emptyList();
  }
}
