package com.matera.keycloak.api;

import com.google.auto.service.AutoService;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

@AutoService({RealmResourceProviderFactory.class })
public class UserRestResourceProviderFactory implements RealmResourceProviderFactory {

    public static final String ID = "get-users-by-session";

    @Override
    public String getId() {

        return ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {

        return new UserRestResourceProvider(session);
    }

    @Override
    public void init(Scope config) { }

    @Override
    public void postInit(KeycloakSessionFactory factory) { }

    @Override
    public void close() { }

}