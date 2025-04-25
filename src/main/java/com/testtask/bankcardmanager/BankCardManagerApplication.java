package com.testtask.bankcardmanager;

import com.testtask.bankcardmanager.config.DotenvApplicationContextInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankCardManagerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BankCardManagerApplication.class);
        app.addInitializers(new DotenvApplicationContextInitializer());
        app.run(args);
    }
}