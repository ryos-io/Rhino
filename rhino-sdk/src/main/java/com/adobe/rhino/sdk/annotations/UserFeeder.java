/**************************************************************************
 *                                                                        *
 * ADOBE CONFIDENTIAL                                                     *
 * ___________________                                                    *
 *                                                                        *
 *  Copyright 2018 Adobe Systems Incorporated                             *
 *  All Rights Reserved.                                                  *
 *                                                                        *
 * NOTICE:  All information contained herein is, and remains              *
 * the property of Adobe Systems Incorporated and its suppliers,          *
 * if any.  The intellectual and technical concepts contained             *
 * herein are proprietary to Adobe Systems Incorporated and its           *
 * suppliers and are protected by trade secret or copyright law.          *
 * Dissemination of this information or reproduction of this material     *
 * is strictly forbidden unless prior written permission is obtained      *
 * from Adobe Systems Incorporated.                                       *
 **************************************************************************/

package com.adobe.rhino.sdk.annotations;

import com.adobe.rhino.sdk.users.DefaultUserRepositoryFactoryImpl;
import com.adobe.rhino.sdk.users.DefaultUserRepositoryImpl;
import com.adobe.rhino.sdk.users.IMSUserRepositoryFactoryImpl;
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
 *
 * @author <a href="mailto:bagdemir@adobe.com">Erhan Bagdemir</a>
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface UserFeeder {

  /**
   * Maximum number of users to be injected.
   *
   * @return Max. number of users.
   */
  int max() default -1;

  /**
   * Factory implementation of {@link UserRepositoryFactory}.
   *
   * @return The class type of the factory.
   */
  Class<? extends UserRepositoryFactory> factory() default DefaultUserRepositoryFactoryImpl.class;
}
