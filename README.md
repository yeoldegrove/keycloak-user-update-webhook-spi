# Keycloak User Update Webhook SPI

This repository provides a custom Service Provider Interface (SPI) for Keycloak, enabling the broadcasting of user profile update events to a webhook. This can be useful for integrating Keycloak with other systems that need to respond to user changes, such as CRM systems, analytics platforms, or any other external services.

## Features

- Broadcasts [UPDATE_PROFILE](https://www.keycloak.org/docs-api/25.0.1/javadocs/org/keycloak/events/EventType.html#UPDATE_PROFILE) events to a configured webhook URL.
- Privacy-friendly and simple body that only includes changed user's `id`.
- Easy to configure and integrate with your existing Keycloak setup.

## Build yourself

1. Clone the repository:
   ```sh
   git clone https://github.com/yeoldegrove/keycloak-user-update-webhook-spi.git
   ```

2. Build the project using Maven:
   ```sh
   cd keycloak-user-update-webhook-spi
   mvn clean install
   ```

## Use released JAR

1. Go to [packages](https://github.com/yeoldegrove/keycloak-user-update-webhook-spi/packages) and get latest `keycloak-user-update-webhook-spi-*.jar` file.

## Installation

1. Copy the generated JAR file to your Keycloak `providers` directory:
   ```sh
   cp keycloak-user-update-webhook-spi-*.jar /opt/keycloak/providers/
   ```

## Enable Event listener in Keycloak

1. Log in to the Keycloak Admin Console.
2. Navigate to `Realm Settings` -> `Events`.
3. Under the `Event listeners` tab, add `user-upadte-webhook`.
4. Save your changes.

## Configuration of SPI

The SPI can be configured via the following options:

1. Using the `spi.events.listener.user.update.webhook.urls` option in the Keycloak configuration.
2. Setting the `KC_SPI_EVENTS_LISTENER_USER_UPDATE_WEBHOOK_URLS` (comma seperated) environment variable.

## Usage

Once configured, any user update events in Keycloak will trigger a request to the configured webhook URL. The payload will simply contain the `id` if the changed user.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request or open an Issue to discuss improvements, bugs, or new features.

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for details.
