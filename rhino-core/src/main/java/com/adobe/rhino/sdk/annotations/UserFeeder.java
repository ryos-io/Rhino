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

import com.adobe.rhino.sdk.users.DefaultUserRepositoryFactoryImpl;
import com.adobe.rhino.sdk.users.UserRepositoryFactory;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark simulation class fields of type {@link com.adobe.rhino.sdk.users.User},
 * that is the injection point where the users will be injected. Users are required to be able
 * to make calls against web services. They might be pre-authenticated according to selected
 * authentication strategy.
 * <p>
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface UserFeeder {

  /**
   * Maximum number of users to be injected.
   * <p>
   *
   * @return Max. number of users.
   */
  int max() default -1;

  /**
   * Delay between login requests while requesting token from IMS.
   * <p>
   *
   * @return Delay in millis.
   */
  long delay() default 0;

  /**
   * Factory implementation of {@link UserRepositoryFactory}.
   * <p>
   *
   * @return The class type of the factory.
   */
  Class<? extends UserRepositoryFactory> factory() default DefaultUserRepositoryFactoryImpl.class;
}
