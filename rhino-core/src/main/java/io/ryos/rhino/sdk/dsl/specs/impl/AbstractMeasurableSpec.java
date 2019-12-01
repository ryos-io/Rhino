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

import io.ryos.rhino.sdk.dsl.specs.DSLSpec;
import io.ryos.rhino.sdk.dsl.specs.MeasurableSpec;

/**
 * Common specification type implementation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public abstract class AbstractMeasurableSpec extends AbstractDSLItem implements MeasurableSpec {

  private boolean measurementEnabled = true;
  private boolean cumulativeMeasurement = false;

  public AbstractMeasurableSpec(String name) {
    super(name);
  }

  @Override
  public DSLSpec noMeasurement() {
    this.measurementEnabled = false;
    return this;
  }

  @Override
  public DSLSpec cumulative() {
    this.cumulativeMeasurement = true;
    return this;
  }

  @Override
  public String getMeasurementPoint() {
    return super.getName();
  }

  @Override
  public boolean isCumulative() {
    return cumulativeMeasurement;
  }

  @Override
  public boolean isMeasurementEnabled() {
    return measurementEnabled;
  }
}
