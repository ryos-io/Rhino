package io.ryos.rhino.sdk.users.oauth;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ryos.rhino.sdk.exceptions.InvalidAuthResponseException;
import io.ryos.rhino.sdk.users.OAuthResponseData;
import javax.ws.rs.core.Response;

public class OAuthUserTokenResponseDeserializer implements ResponseDeserializer<OAuthResponseData> {

  private final ObjectMapper objectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Override
  public OAuthResponseData deserialize(Response response) {
    return mapToEntity(response.readEntity(String.class));
  }

  private OAuthResponseData mapToEntity(final String responseBody) {
    try {
      return objectMapper.readValue(responseBody, OAuthResponseData.class);
    } catch (Exception t) {
      throw new InvalidAuthResponseException(
          "Cannot map authorization server response to entity type: " + OAuthResponseData.class.getName(),
          t);
    }
  }
}
