package com.keycloak.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;

import java.util.Base64;

@Slf4j
public class UserRestRessource {

    private final KeycloakSession session;
    private final AuthenticationManager.AuthResult auth;

    public UserRestRessource(KeycloakSession session) {
        this.session = session;
        this.auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
        KeycloakContext ctx = this.session.getContext();
        log.info("auth {}",auth);
        log.info("auth session  {}",ctx.getRequestHeaders().getHeaderString("Bearer"));
    }
    @OPTIONS
    @Path("{any:.*}")
    public Response preflight() {
        log.info("......................... preflight");
        HttpRequest request = session.getContext().getContextObject(HttpRequest.class);
        log.info("......................... preflight::{}",request.getUri().toString());
        return Cors.add(request, Response.ok()).auth().preflight().build();
    }
    @GET
    @Path("api/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUsersByAttr(@PathParam("id") String id) {
     //        checkRealmAccess();
        assert  Strings.isNullOrEmpty(id);
        // userLocalStorage(): Get keycloak specific local storage for users.  No cache in front, this api talks directly to database configured for Keycloak
        // users(): Get a cached view of all users in system including  users loaded by UserStorageProviders
        // searchForUser(): Support Attributes since v15.1.0
//        UserModel data = session.users().getUserById(session.getContext().getRealm(),id);
//        KeycloakContext context = session.getContext();
//        String clientId = null;
//        String[] keycloakIdentitySplit = context.getRequestHeaders().getCookies().get("KEYCLOAK_IDENTITY").getValue().split("\\.");
//        context.getRequestHeaders().getCookies().get("KEYCLOAK_IDENTITY").getValue().split("\\.");

//        var jwtSessionInformation = sessionToJWTSessionInformation(decodeBase64(keycloakIdentitySplit[1]));
//        log.info("================== jwtSessionInformation::{}", jwtSessionInformation.getSessionState());
//        if (jwtSessionInformation != null) {
//            clientId = EventEntityRepository.getClientIdBySessionId(jwtSessionInformation.getSessionState(), session.getProvider(JpaConnectionProvider.class).getEntityManager());
//        }
        log.info("......................... Entrou api/{}",id);
        KeycloakContext ctx = this.session.getContext();
        log.info("........ api auth session  {}",ctx.getRequestHeaders().getHeaderString("Bearer"));
        SessionDetailsDTO dto = EventEntityRepository.getClientIdBySessionId(id, session.getProvider(JpaConnectionProvider.class).getEntityManager());
        log.info("================== path::{}", session.getContext().getClient());
        log.info("================== User::{}",dto.getUsername());


        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .entity(dto)
                .build();
    }

    private  String decodeBase64(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }
    private JWTSessionInformation sessionToJWTSessionInformation(String session) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(session, JWTSessionInformation.class);
        } catch (JsonProcessingException e) {
           log.error(e.getMessage());
        }
        return null;
    }

    private void checkRealmAccess() {
        if (auth == null) {
            log.info("!!!!!!!!!!!!!!!!!!!!!!!! auth is null !!!!!!!!!!!!!!!!!!!!!!!!!!!");
            throw new NotAuthorizedException("Bearer");
        } else if (auth.getToken().getRealmAccess() == null || !auth.getToken().getRealmAccess().isUserInRole("fetch_users")) {
            throw new ForbiddenException("Does not have permission to fetch users");
        }
    }
}