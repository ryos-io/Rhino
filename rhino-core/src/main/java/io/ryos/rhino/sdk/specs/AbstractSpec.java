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

package io.ryos.rhino.sdk.specs;

/**
 * Common specification type implementation.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public abstract class AbstractSpec implements Spec {

  private String enclosingSpec;
  private String measurementPoint;
  private boolean measurementEnabled = true;
  private boolean cumulativeMeasurement = false;

  AbstractSpec(String measurement) {
    this.measurementPoint = measurement;
  }

  @Override
  public Spec noMeasurement() {
    this.measurementEnabled = false;
    return this;
  }

  @Override
  public Spec cumulativeMeasurement() {
    this.cumulativeMeasurement = true;
    return this;
  }

  @Override
  public String getMeasurementPoint() {
    return measurementPoint;
  }

  @Override
  public String getTestName() {
    return enclosingSpec;
  }

  @Override
  public void setTestName(String testName) {
    this.enclosingSpec = testName;
  }

  @Override
  public boolean isCumulativeMeasurement() {
    return cumulativeMeasurement;
  }

  @Override
  public boolean isMeasurementEnabled() {
    return measurementEnabled;
  }

}
