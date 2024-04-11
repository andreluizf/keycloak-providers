package com.keycloak.api;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

@Slf4j
public class UserRestResourceProvider implements RealmResourceProvider {

    public static final String EMPTY_STRING = "";
    public static final String APPLICATION_NAME = "KEYCLOAK";
    public static final String MIP_APPLICATION_NAME = "MIP";
    public static final String PLATAFORM_ID = "Brazil CBP";
    public static final String CONSOLE_CLIENT_ID = "security-admin-console";
    public static final String MIP_CLIENT_ID = "MIP-Login";
    private static final String REGEX_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    private KeycloakSession session;

    public UserRestResourceProvider(KeycloakSession session) {

        this.session = session;
    }

    @Override
    public Object getResource() {

        return new UserRestRessource(session);
    }

    @Override
    public void close() { }
}