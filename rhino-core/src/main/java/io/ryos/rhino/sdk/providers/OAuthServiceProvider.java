package io.ryos.rhino.sdk.providers;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.users.oauth.OAuthService;
import io.ryos.rhino.sdk.users.oauth.OAuthServiceAuthenticatorImpl;
import io.ryos.rhino.sdk.users.oauth.OAuthServiceTokenResponseDeserializer;

public class OAuthServiceProvider implements Provider<OAuthService> {

    private OAuthService oAuthService;

    public OAuthServiceProvider() {
        var serviceData = new OAuthService();
        serviceData.setGrantType(SimulationConfig.getServiceGrantType());
        serviceData.setClientCode(SimulationConfig.getServiceClientCode());
        serviceData.setClientSecret(SimulationConfig.getServiceClientSecret());
        serviceData.setClientId(SimulationConfig.getServiceClientId());
        this.oAuthService = new OAuthServiceAuthenticatorImpl(new OAuthServiceTokenResponseDeserializer()).authenticate(serviceData);
    }

    @Override
    public OAuthService take() {
        return oAuthService;
    }
}
