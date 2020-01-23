/*
 * Copyright 2020 Ryos.io.
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

package io.ryos.rhino.sdk.dsl;

/**
 * Http method specification consists of methods of Http verbs, e.g get, head, post, ...
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface HttpMethodDsl extends MaterializableDslItem {

  HttpRetriableDsl get();

  HttpRetriableDsl head();

  HttpRetriableDsl put();

  HttpRetriableDsl post();

  HttpRetriableDsl delete();

  HttpRetriableDsl patch();

  HttpRetriableDsl options();
}
