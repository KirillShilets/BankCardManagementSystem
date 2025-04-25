package com.testtask.bankcardmanager.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;
import java.util.stream.Collectors;

public class DotenvApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(DotenvApplicationContextInitializer.class);
    private static final String DOTENV_PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("Initializing environment with .env properties...");
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties()
                .load();

        Map<String, Object> dotenvProperties = dotenv.entries().stream()
                .collect(Collectors.toMap(
                        DotenvEntry::getKey,
                        DotenvEntry::getValue,
                        (existingValue, newValue) -> newValue
                ));

        if (!dotenvProperties.isEmpty()) {
            MapPropertySource dotenvPropertySource = new MapPropertySource(DOTENV_PROPERTY_SOURCE_NAME, dotenvProperties);
            propertySources.addFirst(dotenvPropertySource);

            log.info(".env properties added to Spring Environment as '{}'. Keys loaded: {}", DOTENV_PROPERTY_SOURCE_NAME, dotenvProperties.keySet());
            logKeyPresence(environment, "ENCRYPTION_AES_KEY");
            logKeyPresence(environment, "encryption.aes.key");

        } else {
            log.warn(".env file not found or is empty, skipping adding '{}' property source.", DOTENV_PROPERTY_SOURCE_NAME);
        }
    }

    private void logKeyPresence(ConfigurableEnvironment environment, String key) {
        if (environment.containsProperty(key)) {
            log.info("Key '{}' found in Environment.", key);
        } else {
            log.warn("Key '{}' was NOT found in Environment after initialization.", key);
        }
    }
}