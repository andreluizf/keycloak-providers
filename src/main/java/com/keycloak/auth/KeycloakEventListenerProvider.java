package com.keycloak.auth;


import jakarta.ws.rs.core.NewCookie;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.ServicesLogger;

import java.net.URI;
import java.util.List;
@Slf4j
public class KeycloakEventListenerProvider implements EventListenerProvider {

	private KeycloakSession session;

	public KeycloakEventListenerProvider(KeycloakSession session) {
		this.session = session;
	}


	@Override
	public void close() {
		
	}
	
	@Override
	public void onEvent(Event event) {
		if (List.of(EventType.LOGIN, EventType.LOGOUT).contains(event.getType())) {

			KeycloakContext context = session.getContext();
			URI uri = context.getUri().getBaseUriBuilder().path("realms").path(context.getRealm().getName()).build();
			log.info("=================== entrou  ");
			String details= "{user:absc, clientid:12354, other:12335";
			NewCookie cookie = new NewCookie.Builder("USER_AGENT_COOKIE_NAME")
					.path("/")
					.domain( uri.getPath())
					.value(details)
					.secure(true)
					.build();
			log.info("=================== cookie {}  ",cookie.toString());
			session.getContext().getHttpResponse().setCookieIfAbsent(cookie);
		}
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		try {
			if (List.of(ResourceType.REALM_ROLE_MAPPING, ResourceType.GROUP_MEMBERSHIP, ResourceType.REALM_ROLE_MAPPING).contains(event.getResourceType())) {
				log.info("---------------- admin entrou ");
			}
			if (ResourceType.USER.equals(event.getResourceType())) {
				if (OperationType.UPDATE.equals(event.getOperationType())) {
					log.info("++++++++++++++++++ admin user  entrou ");
				}
			}
			if (ResourceType.GROUP_MEMBERSHIP.equals(event.getResourceType())) {
				log.info("******************* GROUP_MEMBERSHIP user  entrou ");
			}
		} catch (Exception e) {
			ServicesLogger.LOGGER.error(e);
		}
    }
}
