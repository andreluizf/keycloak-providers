package com.matera.keycloak.auth;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
//@AutoService({EventListenerProviderFactory.class })
public class KeycloakEventListenerProviderFactory implements EventListenerProviderFactory {

	@Override
	public EventListenerProvider create(KeycloakSession session) {
        return new KeycloakEventListenerProvider(session);
	}

	@Override
	public void init(Config.Scope config) {

	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
		
	}

	@Override
	public void close() {
		
	}

	@Override
	public String getId() {
		return "keycloak_cookie_event";
	}

}
