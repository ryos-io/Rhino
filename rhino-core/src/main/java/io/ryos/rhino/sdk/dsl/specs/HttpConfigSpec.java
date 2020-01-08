package io.ryos.rhino.sdk.dsl.specs;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.users.data.User;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Configurable Http spec.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
public interface HttpConfigSpec extends HttpMethodSpec {

  HttpConfigSpec endpoint(String endpoint);

  HttpConfigSpec endpoint(Function<UserSession, String> endpoint);

  HttpConfigSpec endpoint(BiFunction<UserSession, HttpSpec, String> endpoint);

  /**
   * Adds a new header into headers.
   * <p>
   *
   * @param headerFunction Function to get the header value.
   * @return {@link HttpSpec} instance with headers initialized.
   */
  HttpConfigSpec header(Function<UserSession, Entry<String, List<String>>> headerFunction);

  HttpConfigSpec header(String key, List<String> values);

  HttpConfigSpec header(String key, String value);

  HttpConfigSpec formParam(Function<UserSession, Entry<String, List<String>>> formParamFunction);

  HttpConfigSpec formParam(String key, List<String> values);

  HttpConfigSpec formParam(String key, String value);

  HttpConfigSpec queryParam(Function<UserSession, Entry<String, List<String>>> queryParamFunction);

  HttpConfigSpec queryParam(String key, List<String> values);

  HttpConfigSpec queryParam(String key, String value);

  HttpConfigSpec auth();

  HttpConfigSpec auth(User user);

  HttpConfigSpec auth(Function<UserSession, User> sessionAccessor);

  /**
   * Requires an authorized user in HTTP requests.
   *
   * @param userSupplier User supplier.
   * @return Instance of {@link HttpConfigSpec}.
   */
  HttpConfigSpec auth(Supplier<User> userSupplier);

  HttpConfigSpec upload(final Supplier<InputStream> inputStream);

  Function<UserSession, User> getUserAccessor();

  Supplier<User> getUserSupplier();
}
