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

package io.ryos.rhino.sdk.users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ryos.rhino.sdk.users.data.User;
import io.ryos.rhino.sdk.users.data.UserImpl;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Vault implementation of {@link UserParser}.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class VaultUserParserImpl implements UserParser {

  private static final Logger LOG = LogManager.getLogger(VaultUserParserImpl.class);
  private static final String DATA_NODE = "data";
  private static final String KEY_USER = "user";
  private static final String KEY_PASS = "pass";
  private static final String KEY_SCOPE = "scope";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public List<User> unmarshal(final InputStream inputStream) {
    var userList = new ArrayList<User>();
    try (final InputStream is = inputStream) {
      JsonNode jsonNode = objectMapper.readTree(is);
      var key = "users";
      JsonNode path = jsonNode.path(DATA_NODE).path(DATA_NODE).path(key);
      if (!path.isNull() && path.isValueNode()) {
        JsonNode payload = objectMapper.readTree(path.textValue());
        Iterator<JsonNode> elements = payload.elements();
        while (elements.hasNext()) {
          JsonNode next = elements.next();
          userList.add(
              new UserImpl(
                  next.get(KEY_USER).textValue(),
                  next.get(KEY_PASS).textValue(),
                  "user-" + UUID.randomUUID(),
                  next.get(KEY_SCOPE).textValue()));
        }
      }
    } catch (IOException e) {
      LOG.error(e);
    }
    return userList;
  }
}
