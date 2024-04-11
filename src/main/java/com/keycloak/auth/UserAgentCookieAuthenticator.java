
package com.keycloak.auth;

import jakarta.ws.rs.core.NewCookie;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.cookie.CookieType;
import org.keycloak.cookie.DefaultCookieProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.*;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.security.PrivateKey;

@Slf4j
public class UserAgentCookieAuthenticator implements Authenticator {


    public static final String USER_AGENT_COOKIE_NAME = "KC_USER_AGENT";

    protected boolean validateCookie(AuthenticationFlowContext context) {
        log.info("============= validateCookie :: PASSOU ");
        Cookie cookie = context.getHttpRequest().getHttpHeaders().getCookies().get(USER_AGENT_COOKIE_NAME);

        if (cookie != null) {
            log.info("============= validateCookie :: not null "+cookie.getValue());
            String encryptedToken = getUserAgentId(context);
            String encryptedCookieValue = cookie.getValue();
            if (encryptedCookieValue!=null && encryptedCookieValue.equals(encryptedToken)){
                log.debug(USER_AGENT_COOKIE_NAME + " cookie is set and valid.");
            }
        }
        return false;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        log.info("=============== autenticate");
//        if (validateCookie(context)) {
            setCookie(context);
            context.success();
//        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        setCookie(context);

    }

    protected String encryptToken(String value, PrivateKey key){
        return new JWSBuilder().jsonContent(value).rsa256(key);
    }

    protected String getUserAgentId(AuthenticationFlowContext context){
        MultivaluedMap<String, String> headers = context.getHttpRequest().getHttpHeaders().getRequestHeaders();
        String username = context.getUser().getUsername();
        String userAgent = headers.getFirst("User-Agent");
        String userIP = headers.getFirst("X-Forwarded-For");
        if (userIP == null){
            userIP = headers.getFirst("Remote-Addr");
        }
        return username + "_" + userIP + "_" +userAgent;
    }

    protected void setCookie(AuthenticationFlowContext context) {
        log.info("================= set coockie ");
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        int maxCookieAge = 60 * 60 * 24 * 30; // 30 days
        if (config != null) {
            maxCookieAge = Integer.valueOf(config.getConfig().get("cookie.max.age"));
        }
        URI uri = context.getUriInfo().getBaseUriBuilder().path("realms").path(context.getRealm().getName()).build();

        PrivateKey key = context.getSession().keys().getActiveRsaKey(context.getRealm()).getPrivateKey();
        String userAgentId = getUserAgentId(context);
        String encryptedValue = encryptToken(userAgentId, key);
        log.info("=================== userAgentId :: {},encryptedValue:: {}  ",userAgentId, encryptedValue);
        String details= "{user:absc, clientid:12354, other:12335";
        NewCookie cookie = new NewCookie.Builder(USER_AGENT_COOKIE_NAME)
                .path("/auth/admin/master/console/")
                .domain( "http://localhost:8084")
                .value(details)
                .secure(true)
                .build();
        KeycloakSession session = context.getSession();
        HttpResponse httpResponse = session.getContext().getHttpResponse();
        httpResponse.setCookieIfAbsent(cookie);
        DefaultCookieProvider cookies = new DefaultCookieProvider(context.getSession().getContext(),false);

        cookies.set(CookieType.LOGIN_HINT,details);
        log.info("=================== cookie::::: {}",cookie.toString());


    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}