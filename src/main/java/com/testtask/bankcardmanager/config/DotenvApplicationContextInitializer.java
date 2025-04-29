package com.testtask.bankcardmanager.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;
import java.util.stream.Collectors;

public class DotenvApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final String DOTENV_PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
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
        }
    }
}