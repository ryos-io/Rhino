package io.ryos.rhino.sdk.users.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a service of the user. If the service exists, then requests against the web
 * service being tests will be made with both user and service token.
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.6.0
 */
public class OAuthService {

  private String clientId;
  private String clientSecret;
  private String grantType;
  private String clientCode;

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("refresh_token")
  private String refreshToken;

  public void setClientId(final String clientId) {
    this.clientId = clientId;
  }

  public void setClientSecret(final String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public void setGrantType(final String grantType) {
    this.grantType = grantType;
  }

  public void setClientCode(final String clientCode) {
    this.clientCode = clientCode;
  }

  public void setAccessToken(final String accessToken) {
    this.accessToken = accessToken;
  }

  public void setRefreshToken(final String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getGrantType() {
    return grantType;
  }

  public String getClientCode() {
    return clientCode;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }
}
