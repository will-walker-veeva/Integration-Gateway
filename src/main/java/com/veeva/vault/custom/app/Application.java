package com.veeva.vault.custom.app;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeva.vault.custom.app.admin.AppConfiguration;
import com.veeva.vault.custom.app.admin.Processor;
import com.veeva.vault.custom.app.admin.Session;
import com.veeva.vault.custom.app.repository.VaultConfigurationRepository;
import com.veeva.vault.custom.app.repository.VaultSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Executor;

@SpringBootApplication(scanBasePackages={
        "com.veeva.vault.custom", "com.veeva.vault.custom.app.admin", "com.veeva.vault.custom.app.repository", "com.veeva.vault.custom.app", "com.veeva.vault.custom.app.client", "com.veeva.vault.custom.app.model"})
@EnableScheduling
@EnableAsync
public class Application implements CommandLineRunner {

    @Autowired
    VaultConfigurationRepository configurationRepository;

    @Autowired
    VaultSessionRepository sessionRepository;

    @Autowired
    AppConfiguration appConfiguration;

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
        String userHomeDir = System.getProperty("user.home");
        File configFile = new File(userHomeDir+"/config.json");
        if(configFile.exists()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
                String content = Files.readString(configFile.toPath());
                List<Processor> configList = objectMapper.readValue(content, new TypeReference<List<Processor>>() {
                });
                configurationRepository.saveAll(configList);
                configFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File sessionFile = new File(userHomeDir+"/sessions.json");
        if(sessionFile.exists()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
                String content = Files.readString(sessionFile.toPath());
                List<Session> sessionList = objectMapper.readValue(content, new TypeReference<List<Session>>() {
                });
                sessionRepository.saveAll(sessionList);
                sessionFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}