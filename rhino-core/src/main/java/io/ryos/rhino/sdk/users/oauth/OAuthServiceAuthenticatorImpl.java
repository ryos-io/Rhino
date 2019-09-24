package io.ryos.rhino.sdk.users.oauth;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import io.ryos.rhino.sdk.exceptions.ServiceLoginException;
import io.ryos.rhino.sdk.exceptions.UserLoginException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthServiceAuthenticatorImpl implements ServiceAuthenticator {

  private static final Logger LOG = LoggerFactory.getLogger(OAuthServiceAuthenticatorImpl.class);

  private static final String CLIENT_ID = "client_id";
  private static final String CLIENT_SECRET = "client_secret";
  private static final String GRANT_TYPE = "grant_type";
  private static final String CODE = "code";

  private OAuthServiceTokenResponseDeserializer deserializer;

  public OAuthServiceAuthenticatorImpl(OAuthServiceTokenResponseDeserializer deserializer) {
    this.deserializer = deserializer;
  }

  @Override
  public OAuthService authenticate(final OAuthService service) {

    try {
      if (!SimulationConfig.isServiceAuthenticationEnabled()) {
        return null;
      }

      var form = createFormObject(service);
      var response = executeRequest(form);

      handleNonOK(service, response);

      var responseServiceEntity = deserializer.deserialize(response);
      service.setAccessToken(responseServiceEntity.getAccessToken());
      service.setRefreshToken(responseServiceEntity.getRefreshToken());
      return service;

    } catch (Exception e) {
      ExceptionUtils.rethrow(e, UserLoginException.class, "Login failed.");
    }

    return null;
  }

  private Response executeRequest(Form form) {
    var client = ClientBuilder.newClient();
    return client
        .target(SimulationConfig.getAuthServer())
        .request()
        .post(Entity.form(form));
  }

  private Form createFormObject(OAuthService service) {
    var form = new Form();

    if (service.getClientId() != null) {
      form.param(CLIENT_ID, service.getClientId());
    }

    if (service.getClientSecret() != null) {
      form.param(CLIENT_SECRET, service.getClientSecret());
    }

    if (service.getGrantType() != null) {
      form.param(GRANT_TYPE, service.getGrantType());
    }

    if (service.getClientCode() != null) {
      form.param(CODE, service.getClientCode());
    }
    return form;
  }

  private void handleNonOK(OAuthService service, Response response) {
    if (response.getStatus() != Status.OK.getStatusCode()) {
      if (LOG.isInfoEnabled()) {
        LOG.info("Cannot login the service, status={}", response.getStatus());
      }
      throw new ServiceLoginException("Cannot authenticate the client: " + service.getClientId());
    }
  }
}
