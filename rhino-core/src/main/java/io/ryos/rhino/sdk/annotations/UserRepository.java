package io.ryos.rhino.sdk.annotations;

import io.ryos.rhino.sdk.users.repositories.DefaultUserRepositoryFactoryImpl;
import io.ryos.rhino.sdk.users.repositories.UserRepositoryFactory;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface UserRepository {
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
   * @return The type of the repository factory.
   */
  Class<? extends UserRepositoryFactory> factory() default DefaultUserRepositoryFactoryImpl.class;
}
