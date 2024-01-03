package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.admin.CacheContext;
import com.veeva.vault.custom.app.exception.AuthenticationException;
import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.admin.Log;
import com.veeva.vault.custom.app.model.files.File;
import com.veeva.vault.custom.app.repository.ContextRepository;
import com.veeva.vault.vapil.api.client.VaultClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The Client is available in script as 'client' variable
 */
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
    @Autowired
    private QueryClient queryClient;
    @Autowired
    private ContextRepository contextRepository;

    /**
     * @hidden
     */
    protected Client(){

    }

    /**
     * @hidden
     * @param autowiredClient
     * @param requestProcessorId
     */
    protected Client(Client autowiredClient, String requestProcessorId){
        this(autowiredClient, requestProcessorId, null, null);
    }

    /**
     * @hidden
     * @param autowiredClient
     * @param requestProcessorId
     * @param vaultDns
     * @param vaultSessionId
     */
    protected Client(Client autowiredClient, String requestProcessorId, String vaultDns, String vaultSessionId){
        this.requestProcessorId = requestProcessorId;
        if(autowiredClient!=null) {
            this.encryptionClient = autowiredClient.encryptionClient;
            this.emailClient = autowiredClient.emailClient;
            this.queryClient = autowiredClient.queryClient;
        }
        if(vaultDns!=null&&vaultSessionId!=null)
            this.vaultClient = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(vaultDns).withVaultSessionId(vaultSessionId).withVaultClientId(VAULT_CLIENT_ID).withValidation(true).withApiErrorLogging(true).withHttpTimeout(120).build();
    }

    /**
     * Returns an initialised Http Client for HTTP operations
     * @return HttpClient
     */
    public HttpClient http(){
        return HttpClient.newInstance();
    }

    /**
     * Returns a new instance of the Template Processing Client for XML/HTML/JSON template parsing
     * @return TemplateProcessorClient
     */
    public TemplateProcessorClient templateProcessor(){
        return TemplateProcessorClient.newInstance();
    }

    /**
     * Returns an authenticated VaultClient when the Processor Type is Spark Message, Web Action Processor, Job Processor or Standard Rest Processor when the Authentication Method is Session ID
     * @return Authenticated VaultClient instance
     */
    public VaultClient vapil(){
        return this.vaultClient;
    }

    /**
     * Returns an initialised Files Client for File operations
     * @return FilesClient
     */
    public FilesClient files(){
        return new FilesClient(this);
    }

    /**
     * Returns an initialised Encryption Client for Encryption operations
     * @return EncryptionClient
     */
    public EncryptionClient encryption(){
        return this.encryptionClient;
    }
    public QueryClient query(){
        return this.queryClient;
    }

    /**
     * Returns an initialised Email Client for Email operations
     * @return EmailClient
     */
    public EmailClient emails(){
        return this.emailClient;
    }

    /**
     * Returns an initialised XmlClient for XML operations
     * @return XmlClient
     */
    public XmlClient xml(){
        return new XmlClient();
    }

    /**
     * Returns an initialised JsonClient for JSON operations
     * @return JsonClient
     */
    public JsonClient json(){
        return new JsonClient();
    }

    /**
     * Returns an initialised CsvClient for CSV operations
     * @return CsvClient
     */
    public CsvClient csv(){
        return new CsvClient();
    }

    /**
     * @hide
     * @param file
     */
    protected void registerFile(File file){
        this.files.add(file);
    }

    /**
     * Use to delete all temporary files created as part of the processor
     * @return true if all deletions were successful
     */
    public boolean deleteAllFiles(){
        return this.files.stream().map(each -> each.delete()).allMatch(each -> each==true);
    }

    /**
     * @hidden
     * @return Logs
     */
    public List<Log> getLogs() {
        return logs;
    }

    /**
     * @hide
     * @param logs
     */
    protected void setLogs(List<Log> logs) {
        this.logs = logs;
    }

    /**
     * Authenticates VAPIL client, can be used to then access the cache context
     * @param vaultDns
     * @param vaultUsername
     * @param vaultPassword
     */
    public void authenticate(String vaultDns, String vaultUsername, String vaultPassword){
        this.vaultClient = VaultClient.newClientBuilder(VaultClient.AuthenticationType.BASIC).withVaultDNS(vaultDns).withVaultUsername(vaultUsername).withVaultPassword(vaultPassword).withVaultClientId(VAULT_CLIENT_ID).withValidation(true).withApiErrorLogging(true).withHttpTimeout(120).build();
    }

    /**
     * Authenticates VAPIL client, can be used to then access the cache context
     * @param vaultDns
     * @param vaultSessionId
     */
    public void authenticate(String vaultDns, String vaultSessionId){
        this.vaultClient = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(vaultDns).withVaultSessionId(vaultSessionId).withVaultClientId(VAULT_CLIENT_ID).withValidation(true).withApiErrorLogging(true).withHttpTimeout(120).build();
    }

    /**
     * Sets a given key's value into the Cache Context for a given Vault DNS. VAPIL must be authenticated to access the Cache Context.
     * @param key
     * @param value
     * @throws ProcessException
     */
    public void setCacheContextValue(String key, Object value) throws AuthenticationException {
        if(this.vaultClient.getAuthenticationResponse().isSuccessful()){
            CacheContext context = contextRepository.findById(this.vaultClient.getVaultDNS()).orElse(new CacheContext(this.vaultClient.getVaultDNS()));
            context.put(key, value);
            contextRepository.save(context);
        }else{
            throw new AuthenticationException("Unauthorised");
        }
    }

    /**
     * Retrieves a given key's value from the Cache Context for a given Vault DNS. VAPIL must be authenticated to access the Cache Context.
     * @param key
     * @param className
     * @return
     * @param <T>
     * @throws ProcessException
     */
    public <T> T getCacheContextValue(String key, Class<T> className) throws AuthenticationException {
        if(this.vaultClient.getAuthenticationResponse().isSuccessful()){
            CacheContext context = contextRepository.findById(this.vaultClient.getVaultDNS()).orElse(new CacheContext(this.vaultClient.getVaultDNS()));
            return (T) context.get(key);
        }else{
            throw new AuthenticationException("Unauthorised");
        }
    }
}
