/*
  Copyright 2018 Adobe.

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

package com.adobe.rhino.sdk.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation is to mark the methods which are to be run before the user session started.
 * The prepare methods are useful to allocate resources and shared among scenarios run by the
 * same user. The test developers might create some resources in prepare method, and scenarios
 * access the same resources without having need of creating them repeatedly.
 * <p>
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @see com.adobe.rhino.sdk.data.UserSession
 * @since 1.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Prepare {

}
