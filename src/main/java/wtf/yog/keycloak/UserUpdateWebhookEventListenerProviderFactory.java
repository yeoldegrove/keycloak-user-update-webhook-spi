package wtf.yog.keycloak;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UserUpdateWebhookEventListenerProviderFactory implements EventListenerProviderFactory {

    public static final String PROVIDER_ID = "user-update-webhook";
    public static final String webhookUrlDefault = "http://localhost:8081";
    private List<String> webhookUrls;
    private static final Logger logger = Logger.getLogger(UserUpdateWebhookEventListenerProviderFactory.class.getName());

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new UserUpdateWebhookEventListenerProvider(session, webhookUrls);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void init(Scope config) {
        // Log the environment variable directly
        String envWebhookUrls = System.getenv("KC_SPI_EVENTS_LISTENER_USER_UPDATE_WEBHOOK_URLS");
        logger.info("Environment variable KC_SPI_EVENTS_LISTENER_USER_UPDATE_WEBHOOK_URLS: " + envWebhookUrls);

        // Log all configuration properties from MicroProfile Config
        ConfigProvider.getConfig().getConfigSources().forEach(source -> {
            logger.info("ConfigSource: " + source.getName());
            source.getProperties().forEach((key, value) -> logger.info(key + ": " + value));
        });

        // Fetch the webhookUrls from MicroProfile Config
        Optional<String> optionalWebhookUrls = ConfigProvider.getConfig().getOptionalValue("spi.events.listener.user.update.webhook.urls", String.class);
        if (optionalWebhookUrls.isPresent()) {
            webhookUrls = Arrays.asList(optionalWebhookUrls.get().split(","));
            logger.info("Webhook URLs fetched from MicroProfile Config: " + webhookUrls);
        } else if (envWebhookUrls != null) {
            webhookUrls = Arrays.asList(envWebhookUrls.split(","));
            logger.info("Using webhook URLs from environment variable: " + webhookUrls);
        } else {
            webhookUrls = Arrays.asList(webhookUrlDefault);
            logger.warning("Using default webhook URL: " + webhookUrls);
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
            .property()
                .name("webhookUrls")
                .label("Webhook URLs")
                .type(ProviderConfigProperty.STRING_TYPE)
                .helpText("Comma-separated list of URLs to send the webhook events")
                .defaultValue(webhookUrlDefault)
            .add()
            .build();
    }
}