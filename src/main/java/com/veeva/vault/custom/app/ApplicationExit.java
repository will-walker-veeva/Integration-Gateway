package com.veeva.vault.custom.app;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.veeva.vault.custom.app.admin.Processor;
import com.veeva.vault.custom.app.admin.Session;
import com.veeva.vault.custom.app.client.Logger;
import com.veeva.vault.custom.app.repository.VaultConfigurationRepository;
import com.veeva.vault.custom.app.repository.VaultSessionRepository;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ApplicationExit {
    @Autowired
    VaultConfigurationRepository configurationRepository;

    @Autowired
    VaultSessionRepository sessionRepository;

    @PreDestroy
    public void destroy() {
        String userHomeDir = System.getProperty("config.home");
        File configFile = new File(userHomeDir+"/config.json");
        Logger logger = Logger.getLogger(Application.class);
        try {
            ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
            Iterable<Processor> configIterable = configurationRepository.findAll();
            List<Processor> configList = StreamSupport.stream(configIterable.spliterator(), false).collect(Collectors.toList());
            try(OutputStream os = new FileOutputStream(configFile)){
                objectMapper.writeValue(os, configList);
            }

        } catch (Exception e) {
            logger.error("Error writing config file", e);
            e.printStackTrace();
        }
        File sessionFile = new File(userHomeDir+"/sessions.json");
        try {
            ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
            Iterable<Session> sessionIterable = sessionRepository.findAll();
            List<Session> sessionList = StreamSupport.stream(sessionIterable.spliterator(), false).collect(Collectors.toList());
            try(OutputStream os = new FileOutputStream(sessionFile)){
                objectMapper.writeValue(os, sessionList);
            }

        } catch (Exception e) {
            logger.error("Error writing session file", e);
            e.printStackTrace();
        }
    }
}
