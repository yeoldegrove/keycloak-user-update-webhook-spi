package wtf.yog.keycloak;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.List;

public class UserUpdateWebhookEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;
    private final List<String> webhookUrls;
    private static final Logger logger = Logger.getLogger(UserUpdateWebhookEventListenerProviderFactory.class.getName());

    public UserUpdateWebhookEventListenerProvider(KeycloakSession session, List<String> webhookUrls) {
        this.session = session;
        this.webhookUrls = webhookUrls;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType().equals(EventType.UPDATE_PROFILE)) {
            handleUserUpdate(event.getUserId());
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        if (event.getOperationType().equals(OperationType.UPDATE) && event.getResourcePath().startsWith("users/")) {
            String userId = event.getResourcePath().split("/")[1];
            handleUserUpdate(userId);
        }
    }

    private void handleUserUpdate(String userId) {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, userId);
        if (user != null) {
            session.getTransactionManager().enlistAfterCompletion(new KeycloakTransaction() {
                @Override
                public void begin() {
                    // No-op
                }

                @Override
                public void commit() {
                    sendWebhook(userId);
                }

                @Override
                public void rollback() {
                    // No-op
                }

                @Override
                public void setRollbackOnly() {
                    // No-op
                }

                @Override
                public boolean getRollbackOnly() {
                    return false;
                }

                @Override
                public boolean isActive() {
                    return true;
                }
            });
        }
    }

    private void sendWebhook(String id) {
        for (String webhookUrl : webhookUrls) {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");

                String jsonInputString = "{\"id\": \"" + id + "\"}";
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                logger.info("Webhook response code: " + code);
            } catch (IOException e) {
                logger.severe("Cannot connect to webhook URL: " + webhookUrl);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {}
}
