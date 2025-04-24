package com.testtask.bankcardmanager;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class BankCardManagerApplication {

    private static final Logger log = LoggerFactory.getLogger(BankCardManagerApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BankCardManagerApplication.class);
        app.addInitializers(new ApplicationContextInitializer<ConfigurableApplicationContext>() {
            @Override
            public void initialize(ConfigurableApplicationContext applicationContext) {
                log.info("Initializing environment with .env properties...");
                Dotenv dotenv = Dotenv.configure()
                        .ignoreIfMissing()
                        .load();
                Map<String, Object> dotenvProperties = dotenv.entries().stream()
                        .collect(Collectors.toMap(DotenvEntry::getKey, DotenvEntry::getValue));

                if (!dotenvProperties.isEmpty()) {
                    ConfigurableEnvironment environment = applicationContext.getEnvironment();
                    MutablePropertySources propertySources = environment.getPropertySources();
                    propertySources.addFirst(new MapPropertySource("dotenvProperties", dotenvProperties));
                    log.info(".env properties added to Spring Environment. Keys: {}", dotenvProperties.keySet());
                    if (environment.containsProperty("ENCRYPTION_AES_KEY")) {
                        log.info("Key 'ENCRYPTION_AES_KEY' found in Environment after adding dotenv source.");
                    } else if (environment.containsProperty("encryption.aes.key")) {
                        log.info("Key 'encryption.aes.key' found in Environment after adding dotenv source.");
                    } else {
                        log.warn("Key for encryption was NOT found immediately after adding dotenv source!");
                    }
                } else {
                    log.warn(".env file not found or empty, skipping adding properties to Environment.");
                }
            }
        });

        app.run(args);
    }
}