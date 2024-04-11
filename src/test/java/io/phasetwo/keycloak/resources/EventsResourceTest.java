package io.phasetwo.keycloak.resources;

import static io.phasetwo.keycloak.Helpers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.github.xgp.http.server.Server;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.phasetwo.keycloak.KeycloakSuite;
import io.phasetwo.keycloak.representation.RealmAttributeRepresentation;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.jbosslog.JBossLog;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.ClassRule;
import org.junit.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.broker.provider.util.SimpleHttp;

@JBossLog
public class EventsResourceTest {

  @ClassRule public static KeycloakSuite server = KeycloakSuite.SERVER;

  CloseableHttpClient httpClient = HttpClients.createDefault();

  String webhookUrl() {
    return server.getAuthUrl() + "/realms/master/webhooks";
  }

  String eventsUrl() {
    return server.getAuthUrl() + "/realms/master/events";
  }

  String attributesUrl() {
    return server.getAuthUrl() + "/realms/master/attributes";
  }

  @Test
  public void testWebhookReceivesEvent() throws Exception {
    Keycloak keycloak = server.client();
    // update a realm with the ext-event-webhook listener
    addEventListener(keycloak, "master", "ext-event-webhook");

    AtomicReference<String> body = new AtomicReference<String>();
    // create a server on a free port with a handler to listen for the event
    int port = nextFreePort(8083, 10000);
    createWebhook(
        keycloak,
        httpClient,
        webhookUrl(),
        "http://127.0.0.1:" + port + "/webhook",
        "qlfwemke",
        ImmutableSet.of("admin.*", "foo.*"));

    Server server = new Server(port);
    server
        .router()
        .POST(
            "/webhook",
            (request, response) -> {
              String r = request.body();
              log.infof("%s", r);
              body.set(r);
              response.body("OK");
              response.status(202);
            });
    server.start();
    Thread.sleep(1000l);

    // cause an event to be sent
    Map<String, String> ev = ImmutableMap.of("type", "foo.BAR");
    sendEvent(keycloak, ev);

    Thread.sleep(1000l);

    // check the handler for the event, after a delay
    assertNotNull(body.get());
    assertThat(body.get(), containsString("foo.BAR"));

    removeEventListener(keycloak, "master", "ext-event-webhook");
    server.stop();
  }

  @Test
  public void testHttpConfiguredEvent() throws Exception {
    Keycloak keycloak = server.client();

    AtomicInteger cnt = new AtomicInteger(0);
    AtomicReference<String> body = new AtomicReference<String>();
    // create a server on a free port with a handler to listen for the event
    int port = nextFreePort(8087, 10000);
    Server server = new Server(port);
    server
        .router()
        .POST(
            "/webhook",
            (request, response) -> {
              log.infof("TEST SERVER %s %s", request.method(), request.body());
              if (cnt.get() == 0) {
                response.body("INTERNAL SERVER ERROR");
                response.status(500);
                cnt.incrementAndGet();
              } else {
                String r = request.body();
                log.infof("body %s", r);
                body.set(r);
                response.body("OK");
                response.status(202);
              }
            });
    server.start();
    Thread.sleep(1000l);

    String targetUri = "http://127.0.0.1:" + port + "/webhook";

    // create the config for a http event listener
    String key = "_providerConfig.ext-event-http.0";
    String value = "{ \"targetUri\": \"" + targetUri + "\", \"retry\": false }";
    RealmAttributeRepresentation rep = new RealmAttributeRepresentation();
    rep.setRealm("master");
    rep.setName(key);
    rep.setValue(value);
    SimpleHttp.Response resp =
        SimpleHttp.doPost(attributesUrl(), httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(rep)
            .asResponse();
    assertThat(resp.getStatus(), is(201));

    // update a realm with the ext-event-http listener
    addEventListener(keycloak, "master", "ext-event-http");

    // cause an event to be sent
    Map<String, String> ev = ImmutableMap.of("type", "foo.BAR");
    sendEvent(keycloak, ev);

    // wait
    Thread.sleep(1000l);

    // check the handler for the event, after a delay
    assertThat(cnt.get(), is(1));
    assertNull(body.get());

    /*
    // retry = true
    cnt.set(0);
    value = "{ \"targetUri\": \""+targetUri+"\", \"retry\": true }";
    rep.setValue(value);
    resp =
        SimpleHttp.doPut(attributesUrl() + "/" + urlencode(key), httpClient)
        .auth(keycloak.tokenManager().getAccessTokenString())
        .json(rep)
        .asResponse();
    assertThat(resp.getStatus(), is(204));

    // cause an event to be sent
    sendEvent(keycloak, ev);

    // wait
    Thread.sleep(1000l);

    // check the handler for the event, after a delay
    assertThat(cnt.get(), is(1));
    assertNotNull(body.get());
    assertThat(body.get(), containsString("foo.BAR"));

    */

    removeEventListener(keycloak, "master", "ext-event-http");
    // wait and stop
    Thread.sleep(1000l);
    server.stop();
  }

  SimpleHttp.Response sendEvent(Keycloak keycloak, Map<String, String> ev) throws Exception {
    SimpleHttp.Response resp =
        SimpleHttp.doPost(eventsUrl(), httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(ev)
            .asResponse();
    assertThat(resp.getStatus(), is(202));
    return resp;
  }
}
