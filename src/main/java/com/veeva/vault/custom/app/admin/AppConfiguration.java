package com.veeva.vault.custom.app.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Value("${custom.vault.configurationHost}")
    private String vaultConfigurationHost;

    @Value("${custom.decryptionToken}")
    private String decryptionToken;

    @Value("${custom.email.host:#{null}}")
    private String emailHost;

    @Value("${custom.email.port:#{null}}")
    private Integer emailPort;

    @Value("${custom.email.username:#{null}}")
    private String emailUsername;

    @Value("${custom.email.password:#{null}}")
    private String emailPassword;

    public String getVaultConfigurationHost() {
        return vaultConfigurationHost;
    }

    public String getDecryptionToken(){
        return decryptionToken;
    }

    public String getEmailHost() {
        return emailHost;
    }

    public Integer getEmailPort() {
        return emailPort;
    }

    public String getEmailUsername() {
        return emailUsername;
    }

    public String getEmailPassword() {
        return emailPassword;
    }
}