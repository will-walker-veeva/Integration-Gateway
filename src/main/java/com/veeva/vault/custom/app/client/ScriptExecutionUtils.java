package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.admin.Processor;
import com.veeva.vault.custom.app.client.Client;
import com.veeva.vault.custom.app.client.Logger;
import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.http.HttpRequest;
import com.veeva.vault.custom.app.model.http.HttpResponse;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ScriptExecutionUtils {
    @Autowired
    Client autowiredClient;

    public static final List<String> ALLOWED_LIST = Arrays.asList("java.util.*",
            "java.lang.*", "java.time.*", "java.time.format.*", "java.math.*",
            "java.nio.charset.*", "java.time.temporal.*", "java.time.zone.*", "java.time.chrono.*",
            "java.util.regex.*", "java.util.stream.*", "java.text.*",
            "com.veeva.vault.custom.app.model.csv.*", "com.veeva.vault.custom.app.model.files.*",
            "com.veeva.vault.custom.app.model.json.*", "com.veeva.vault.custom.app.model.http.*",
            "com.veeva.vault.vapil.*", "com.veeva.vault.vapil.api.*", "com.veeva.vault.vapil.api.client.*",
            "com.veeva.vault.vapil.api.model.*", "com.veeva.vault.vapil.api.model.builder.*",
            "com.veeva.vault.vapil.api.model.common.*", "com.veeva.vault.vapil.api.model.metadata.*",
            "com.veeva.vault.vapil.api.model.response.*", "com.veeva.vault.vapil.api.request.*",
            "com.veeva.vault.vapil.api.connector.*");

    private GroovyShell loadScriptContext(){
        final ImportCustomizer imports = new ImportCustomizer().addStarImports("java.util", "java.lang", "java.time", "java.math", "java.time.format",  "com.veeva.vault.custom.app.model.csv",  "com.veeva.vault.custom.app.model.files",  "com.veeva.vault.custom.app.model.json",  "com.veeva.vault.custom.app.model.http", "com.veeva.vault.vapil.api.client", "com.veeva.vault.vapil.api.request");
        final SecureASTCustomizer secure = new SecureASTCustomizer();
        secure.setAllowedStarImports(ALLOWED_LIST);
        secure.setIndirectImportCheckEnabled(true);
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(imports, secure);
        GroovyShell shell = new GroovyShell(config);
        return shell;
    }

    public Client executeScript(HttpRequest request, Processor processor, HttpResponse response){
        Client client = new Client(autowiredClient, processor.getId());
        Logger logger = Logger.getLogger(processor.getId());
        GroovyShell scriptContext = loadScriptContext();
        scriptContext.setVariable("response", response);
        scriptContext.setVariable("request", request);
        scriptContext.setVariable("configuration", processor.getConfiguration());
        if(processor.getProcessorType() == Processor.Type.base__v && processor.getAuthenticationMethod() == Processor.AuthenticationMethod.session_id__c){
            try{
                client = new Client(autowiredClient, processor.getId(), request.getHeaders().get("x-vault-dns"), request.getHeaders().get("authorization"));
            }catch(Exception e){
                logger.error(e.getMessage(), e);
                response.setResponseCode(401);
                response.setResponseBody("FAILURE");
                client = new Client(autowiredClient, processor.getId());
                client.setLogs(logger.getLogs(Thread.currentThread().getName()));
                return client;
            }
        }else if(processor.getProcessorType() == Processor.Type.spark__c){
            try{
                JsonObject jsonObject = new JsonObject(request.getRequestBody());
                String vaultDNS = jsonObject.getString("vault_host_name");
                String sessionId = jsonObject.getJsonObject("message").getJsonObject("attributes").getString("authorization");
                client = new Client(autowiredClient, processor.getId(), vaultDNS, sessionId);
            }catch(Exception e){
                logger.error(e.getMessage(), e);
                response.setResponseCode(401);
                response.setResponseBody("FAILURE");
                client = new Client(autowiredClient, processor.getId());
                client.setLogs(logger.getLogs(Thread.currentThread().getName()));
                return client;
            }
        }else{
            try{
                client = new Client(autowiredClient, processor.getId(), request.getHeaders().get("x-vault-dns"), request.getHeaders().get("authorization"));
            }catch(Exception e){
                logger.error(e.getMessage(), e);
                response.setResponseCode(401);
                response.setResponseBody("FAILURE");
                client = new Client(autowiredClient, processor.getId());
                client.setLogs(logger.getLogs(Thread.currentThread().getName()));
                return client;
            }
        }
        scriptContext.setVariable("client", client);
        scriptContext.setVariable("logger", Logger.getLogger(request.getRequestProcessorId()));
        try {
            scriptContext.evaluate(processor.getDefinition());
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            response.setResponseCode(401);
            response.setResponseBody("FAILURE: "+e.getMessage());
        }
        client.setLogs(logger.getLogs(Thread.currentThread().getName()));
        return client;
    }

    public ScriptValidationResponse newScriptValidationResponseInstance(){
        return new ScriptValidationResponse(true);
    }

    public ScriptValidationResponse validateScript(String script){
        GroovyShell scriptContext = loadScriptContext();
        ScriptValidationResponse response = new ScriptValidationResponse();
        try {
            Script myScript = scriptContext.parse(script);
        }catch(Exception e){
            response = new ScriptValidationResponse(false, e.getMessage());
        }
        return response;
    }


    public class ScriptValidationResponse{
        private boolean validated = true;
        private String validationMessage;

        public ScriptValidationResponse(){

        }

        public ScriptValidationResponse(boolean validated){
            this.validated = validated;
        }

        public ScriptValidationResponse(boolean validated, String validationMessage){
            this.validated = validated;
            this.validationMessage = validationMessage;
        }

        public boolean isValidated() {
            return validated;
        }

        public String getValidationMessage() {
            return validationMessage;
        }
    }

}
