package io.ryos.rhino.sdk.users;

import io.ryos.rhino.sdk.users.data.User;
import java.io.InputStream;
import java.util.List;

public interface UserParser {

  List<User> unmarshall(final InputStream inputStream);
}
