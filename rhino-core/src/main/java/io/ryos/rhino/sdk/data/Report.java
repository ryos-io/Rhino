/*
  Copyright 2018 Ryos.io.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package io.ryos.rhino.sdk.data;

import io.ryos.rhino.sdk.annotations.Scenario;

/**
 * Report value type is used to prepare step results. Create result instances in step
 * methods to report step metrics. The metrics collected in the {@link Report} instances are
 * returned from the step methods, that are annotated with {@link Scenario}.
 *
 * @author Erhan Bagdemir
 * @since 1.0
 */
public class Report {

    /**
     * Status of HTTP request will be written into the benchmark logs. Those are well-known HTTP
     * status codes e.g 200 for HTTP OK, etc.
     */
    private int status;

    /**
     * Status description will be output in the benchmark logs. The description is mostly useful
     * of the request fails.
     */
    private String description;

    private Report(Builder builder) {
        this.status = builder.status;
        this.description = builder.description;
    }

    /**
     * Nested builder type to create new instances of {@link Report}.
     *
     * @author Erhan Bagdemir
     */
    public static class Builder {

        /**
         * Status of HTTP request will be written into the benchmark logs.
         */
        private int status;

        /**
         * Status description will be output in the benchmark logs.
         */
        private String description;

        /**
         * Builder method for status.
         *
         * @param status HTTP status of the step.
         * @return {@link Builder} instance with status.
         */
        public Builder status(int status) {
            this.status = status;
            return this;
        }

        /**
         * Description of the result.
         *
         * @param description Description of the step result.
         * @return {@link Builder} instance with description.
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Builder method to construct the {@link Report} instances.
         *
         * @return A {@link Report} instance.
         */
        public Report build() {
            return new Report(this);
        }
    }

    /**
     * Getter method for the status code.
     *
     * @return HTTP status code.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Getter method for the description.
     *
     * @return Description of the result.
     */
    public String getDescription() {
        return description;
    }
}
