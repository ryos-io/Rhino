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

package io.ryos.rhino.sdk.validators;

import java.util.Properties;

/**
 * @author Erhan Bagdemir
 * @since 2019
 */
public class PropsValidatorImpl implements Validator<Properties> {

  private static final String PACKAGE_TO_SCAN = "packageToScan";

  @Override
  public void validate(Properties props) {
    validatePackageToScan(props);
  }

  private void validatePackageToScan(Properties props) {
    if (props.get(PACKAGE_TO_SCAN) == null) {
      throw new PropsValidationException(PACKAGE_TO_SCAN, PACKAGE_TO_SCAN
          + " property must not be null. Please set packageToScan property in rhino.properties "
          + "file to tell Rhino where to find test entities.");
    }
  }
}
