package com.veeva.vault.custom.app;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.veeva.vault.custom.app.admin.AppConfiguration;
import com.veeva.vault.custom.app.admin.Processor;
import com.veeva.vault.custom.app.admin.Session;

import com.veeva.vault.custom.app.client.Client;
import com.veeva.vault.custom.app.client.Logger;
import com.veeva.vault.custom.app.repository.VaultProcessorRepository;
import com.veeva.vault.custom.app.repository.VaultSessionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Executor;

@SpringBootApplication(scanBasePackages={
        "com.veeva.vault.custom", "com.veeva.vault.custom.app.admin", "com.veeva.vault.custom.app.repository", "com.veeva.vault.custom.app", "com.veeva.vault.custom.app.client", "com.veeva.vault.custom.app.model"})
@EnableScheduling
@EnableAsync
@EnableWebSecurity
public class Application implements CommandLineRunner {

    @Autowired
    VaultProcessorRepository configurationRepository;

    @Autowired
    VaultSessionRepository sessionRepository;

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    Client client;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AsyncJob-");
        executor.initialize();
        return executor;
    }

    @Override
    public void run(String... args) throws Exception {
        Logger logger = Logger.getLogger(Application.class);
        logger.info("Starting application ... ");
    }
}