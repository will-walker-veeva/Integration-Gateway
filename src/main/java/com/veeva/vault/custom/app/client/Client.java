package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.admin.Log;
import com.veeva.vault.custom.app.model.files.File;
import com.veeva.vault.vapil.api.client.VaultClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Client {
    public static final String VAULT_CLIENT_ID = "vps-api-gateway";
    private String requestProcessorId;
    private List<File> files = new ArrayList<File>();
    private List<Log> logs;
    private VaultClient vaultClient;
    @Autowired
    private EncryptionClient encryptionClient;
    @Autowired
    private EmailClient emailClient;

    private Client(){

    }

    public Client(Client autowiredClient, String requestProcessorId){
        this(autowiredClient, requestProcessorId, null, null);
    }

    public Client(Client autowiredClient, String requestProcessorId, String vaultDns, String vaultSessionId){
        this.requestProcessorId = requestProcessorId;
        this.encryptionClient = autowiredClient.encryptionClient;
        this.emailClient = autowiredClient.emailClient;
        if(vaultDns!=null&&vaultSessionId!=null)
            this.vaultClient = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(vaultDns).withVaultSessionId(vaultSessionId).withVaultClientId(VAULT_CLIENT_ID).withValidation(true).withApiErrorLogging(true).withHttpTimeout(120).build();
    }

    public HttpClient http(){
        return HttpClient.newInstance();
    }

    public TemplateProcessorClient templateProcessor(){
        return TemplateProcessorClient.newInstance();
    }

    public VaultClient vapil(){
        return this.vaultClient;
    }

    public FilesClient files(){
        return new FilesClient(this);
    }

    public EncryptionClient encryption(){
        return this.encryptionClient;
    }

    public EmailClient emails(){
        return this.emailClient;
    }

    public XmlClient xml(){
        return new XmlClient();
    }
    protected void registerFile(File file){
        this.files.add(file);
    }
    public boolean deleteAllFiles(){
        return this.files.stream().map(each -> each.delete()).allMatch(each -> each==true);
    }
    public List<Log> getLogs() {
        return logs;
    }
    protected void setLogs(List<Log> logs) {
        this.logs = logs;
    }
}
