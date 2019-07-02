package io.ryos.rhino.sdk.users.repositories;

import io.ryos.rhino.sdk.users.data.OAuthUser;
import io.ryos.rhino.sdk.users.data.User;

/**
 * TODO
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface Authenticator<T extends User> {

  T authenticate(User user);

}
