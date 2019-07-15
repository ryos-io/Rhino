package io.ryos.rhino.sdk.users.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import io.ryos.rhino.sdk.exceptions.UserLoginException;
import io.ryos.rhino.sdk.users.data.OAuthService;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthServiceAuthenticatorImpl implements ServiceAuthenticator {

  private static final Logger LOG = LoggerFactory.getLogger(OAuthServiceAuthenticatorImpl.class);

  private static final String CLIENT_ID = "client_id";
  private static final String CLIENT_SECRET = "client_secret";
  private static final String GRANT_TYPE = "grant_type";
  private static final String CODE = "code";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public OAuthService authenticate(final OAuthService service) {

    try {

      var form = new Form();

      if (SimulationConfig.getClientId() != null) {
        form.param(CLIENT_ID, SimulationConfig.getServiceClientId());
      }

      if (SimulationConfig.getClientSecret() != null) {
        form.param(CLIENT_SECRET, SimulationConfig.getServiceClientSecret());
      }

      if (SimulationConfig.getGrantType() != null) {
        form.param(GRANT_TYPE, SimulationConfig.getServiceGrantType());
      }

      if (SimulationConfig.getClientCode() != null) {
        form.param(CODE, SimulationConfig.getServiceClientCode());
      }

      var client = ClientBuilder.newClient();
      var response = client
          .target(SimulationConfig.getAuthServer())
          .request()
          .post(Entity.form(form));

      if (response.getStatus() != Status.OK.getStatusCode()) {
        LOG.info("Cannot login the service, status={} message={}", response.getStatus(), response.readEntity(String.class));
        return null;
      }

      var responseInString = response.readEntity(String.class);
      LOG.debug(responseInString);

      return mapToEntity(responseInString);
    } catch (Exception e) {
      ExceptionUtils.rethrow(e, UserLoginException.class, "Login failed.");
    }

    return null;
  }

  private OAuthService mapToEntity(final String s) {
    final OAuthService o;
    try {
      o = objectMapper.readValue(s, OAuthService.class);
    } catch (Exception e) {
      throw new RuntimeException("Cannot map authorization server response to entity type: " + OAuthService.class.getName(),
          e);
    }
    return o;
  }
}
