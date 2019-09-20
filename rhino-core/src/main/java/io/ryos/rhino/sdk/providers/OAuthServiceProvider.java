package io.ryos.rhino.sdk.providers;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.users.oauth.OAuthService;
import io.ryos.rhino.sdk.users.oauth.OAuthServiceAuthenticatorImpl;

public class OAuthServiceProvider implements Provider<OAuthService> {

    private OAuthService oAuthService;

    public OAuthServiceProvider() {
        var serviceData = new OAuthService();
        serviceData.setGrantType(SimulationConfig.getServiceGrantType());
        serviceData.setClientCode(SimulationConfig.getServiceClientCode());
        serviceData.setClientSecret(SimulationConfig.getServiceClientSecret());
        serviceData.setClientId(SimulationConfig.getServiceClientId());
        var authenticate = new OAuthServiceAuthenticatorImpl().authenticate(serviceData);
        synchronized (this) {
            this.oAuthService = authenticate;
        }
    }

    @Override
    public synchronized OAuthService take() {
        return oAuthService;
    }
}
