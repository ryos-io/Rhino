package io.ryos.rhino.sdk.users.oauth;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ryos.rhino.sdk.exceptions.InvalidAuthResponseException;
import javax.ws.rs.core.Response;

public class OAuthServiceTokenResponseDeserializer implements ResponseDeserializer<OAuthService> {

  private final ObjectMapper objectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Override
  public OAuthService deserialize(Response response) {
    return mapToEntity(response.readEntity(String.class));
  }

  private OAuthService mapToEntity(final String s) {
    final OAuthService o;
    try {
      o = objectMapper.readValue(s, OAuthService.class);
    } catch (Exception e) {
      throw new InvalidAuthResponseException(
          "Cannot map authorization server response to entity type: " + OAuthService.class
              .getName(), e);
    }
    return o;
  }
}
