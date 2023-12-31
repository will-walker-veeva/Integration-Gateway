package com.veeva.vault.custom.app.client;

import com.veeva.vault.custom.app.admin.AppConfiguration;
import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.admin.Processor;
import com.veeva.vault.custom.app.admin.ScriptLibrary;
import com.veeva.vault.custom.app.model.files.File;
import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.http.HttpRequest;
import com.veeva.vault.custom.app.model.http.HttpResponse;
import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @hidden
 */
@Service
public class ScriptExecutionUtils {

    public ScriptExecutionUtils(){

    }

    @Autowired
    private Client autowiredClient;

    @Autowired
    private AppConfiguration appConfiguration;

    private FilesClient filesClient;

    private Logger logger;

    private static Map<Processor.Environment, GroovyScriptEngine> libraryGroovyShells;

    public static final List<String> ALLOWED_STAR_LIST = Arrays.asList("java.util.*",
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

    public static final List<String> ALLOWED_LIST = Arrays.asList("com.veeva.vault.custom.app.client.Client", "com.veeva.vault.custom.app.client.Emaillient", "com.veeva.vault.custom.app.client.EncryptionClient", "com.veeva.vault.custom.app.client.FilesClient", "com.veeva.vault.custom.app.client.HttpClient", "com.veeva.vault.custom.app.client.JsonClient", "com.veeva.vault.custom.app.client.Logger", "com.veeva.vault.custom.app.client.TemplateProcessorClient", "com.veeva.vault.custom.app.client.XmlClient" );

    private static final String[] AUTO_LIST = {"java.util", "java.lang", "java.time", "java.math", "java.time.format",  "com.veeva.vault.custom.app.model.csv",  "com.veeva.vault.custom.app.model.files",  "com.veeva.vault.custom.app.model.json",  "com.veeva.vault.custom.app.model.http", "com.veeva.vault.vapil.api.client", "com.veeva.vault.vapil.api.request"};

    @Autowired
    public void init() {
        filesClient = new FilesClient();
        logger = Logger.getLogger(this.getClass());
        libraryGroovyShells = new HashMap<Processor.Environment, GroovyScriptEngine>();
        Processor.Environment[] environments = Processor.Environment.values();
        Arrays.stream(environments).forEach(environmentType -> {
            String path = getCachePath(environmentType);
            GroovyScriptEngine scriptEngine = null;
            try {
                scriptEngine = new GroovyScriptEngine(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<java.io.File> scriptFiles = Arrays.asList(new java.io.File(path).listFiles());
            Set<String> classNames = scriptFiles.stream().map(each -> each.getName()).map(each -> each.replaceAll("^(.*).groovy$", "$1")).collect(Collectors.toSet());
            scriptEngine.setConfig(getCompilerConfiguration(environmentType, classNames));
            List<java.io.File> failedFiles = new ArrayList<java.io.File>();
            int attempts = 0;
            while (!scriptFiles.isEmpty() && !failedFiles.isEmpty() || !failedFiles.isEmpty() && attempts < 5) {
                for (java.io.File file : failedFiles) {
                    try {
                        scriptEngine.getGroovyClassLoader().parseClass(new GroovyCodeSource(file), true);
                        failedFiles.remove(file);
                    } catch (Exception e) {

                    }
                }
                for (java.io.File file : scriptFiles) {
                    try {
                        scriptEngine.getGroovyClassLoader().parseClass(new GroovyCodeSource(file), true);
                        scriptFiles.remove(file);
                    } catch (Exception e) {
                        failedFiles.add(file);
                    }
                }
                attempts++;
            }
            if (!failedFiles.isEmpty()) {
                failedFiles.stream().forEach(file -> {
                    logger.error("Failed to load {}", file.getName());
                });
            }
            libraryGroovyShells.put(environmentType, scriptEngine);
        });
    }

    protected GroovyScriptEngine loadScriptContext(Processor.Environment environmentType){
        CompilerConfiguration config = getCompilerConfiguration(environmentType);
        GroovyScriptEngine engine = libraryGroovyShells.get(environmentType);
        engine.setConfig(config);
        return engine;
    }

    protected GroovyScriptEngine loadScriptContext(Processor.Environment environmentType, Collection<String> deployedClasses) {
        CompilerConfiguration config = getCompilerConfiguration(environmentType, deployedClasses);
        GroovyScriptEngine engine = libraryGroovyShells.get(environmentType);
        engine.setConfig(config);
        return engine;
    }

    private CompilerConfiguration getCompilerConfiguration(Processor.Environment environmentType){
        final ImportCustomizer imports = new ImportCustomizer().addStarImports(AUTO_LIST);
        final SecureASTCustomizer secure = new SecureASTCustomizer();
        secure.setAllowedStarImports(ALLOWED_STAR_LIST);
        secure.setIndirectImportCheckEnabled(true);
        secure.setAllowedImports(ALLOWED_LIST);
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(imports, secure);
        config.setTargetDirectory(getCachePath(environmentType));
        return config;
    }

    private CompilerConfiguration getCompilerConfiguration(Processor.Environment environmentType, Collection<String> supportingClasses){
        final ImportCustomizer imports = new ImportCustomizer().addStarImports(AUTO_LIST);
        supportingClasses.stream().forEach(supportingClass -> imports.addImports(supportingClass));
        final SecureASTCustomizer secure = new SecureASTCustomizer();
        secure.setAllowedStarImports(ALLOWED_STAR_LIST);
        secure.setIndirectImportCheckEnabled(true);
        secure.setAllowedImports(Stream.concat(ALLOWED_LIST.stream(), supportingClasses.stream()).collect(Collectors.toList()));
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(imports, secure);
        config.setTargetDirectory(getCachePath(environmentType));
        return config;
    }

    public Client executeScript(HttpRequest request, Processor processor, HttpResponse response){
        Client client = new Client(autowiredClient, processor.getId());
        Logger logger = Logger.getLogger(processor.getId());
        String environmentType = processor.getEnvironmentType().toLabel();
        Set<String> scriptLibraryClasses = processor.getScriptLibraryHolder()!=null && processor.getScriptLibraryHolder().getScriptLibraries()!=null? processor.getScriptLibraryHolder().getScriptLibraries().stream().map(scriptLibrary -> scriptLibrary.getPackageName()+"."+scriptLibrary.getClassName()).collect(Collectors.toSet()) : null;
        GroovyScriptEngine scriptEngine = scriptLibraryClasses!=null? loadScriptContext(processor.getEnvironmentType(), scriptLibraryClasses) : loadScriptContext(processor.getEnvironmentType());
        Binding scriptContext = new Binding();
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
            scriptEngine.run(processor.getId()+".groovy", scriptContext);
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            response.setResponseCode(401);
            response.setResponseBody("FAILURE: "+e.getMessage());
        }
        client.setLogs(logger.getLogs(Thread.currentThread().getName()));
        return client;
    }

    public ScriptValidationResponse newScriptValidationResponseInstance(){
        return new ScriptValidationResponse(true);
    }

    public ScriptValidationResponse validateScript(Processor.Environment environmentType, String script, List<String> dependencies){
        ScriptValidationResponse response = new ScriptValidationResponse();
        try {
            Set<String> convertedDependencies = dependencies.stream().map(each -> convertValidatedNameToLibraryName(each)).collect(Collectors.toSet());
            logger.debug("Validating script with dependencies {}", convertedDependencies);
            GroovyScriptEngine scriptEngine = loadScriptContext(environmentType, convertedDependencies);
            GroovyShell shell = new GroovyShell(scriptEngine.getGroovyClassLoader(), scriptEngine.getConfig());
            Script myScript = shell.parse(script);
        }catch(Exception e){
            response = new ScriptValidationResponse(false, e.getMessage());
        }
        return response;
    }

    public ScriptValidationResponse deployScript(Processor processor){
        ScriptValidationResponse response = new ScriptValidationResponse();
        String libraryName = processor.getId();
        java.io.File javaFile = new java.io.File(getCachePath(processor.getEnvironmentType())+libraryName+".groovy");
        try {
            File file = new File(javaFile);
            this.filesClient.writeStringToFile(file, processor.getDefinition(), StandardCharsets.UTF_8);
        }catch(ProcessException e){
            e.printStackTrace();
            response = new ScriptValidationResponse(false, e.getMessage());
        }
        return response;
    }

    public ScriptValidationResponse deployLibrary(ScriptLibrary scriptLibrary){
        ScriptValidationResponse response = new ScriptValidationResponse();
        String libraryName = scriptLibrary.getValidatedName();
        java.io.File javaFile = convertLibraryNameToFile(libraryName);
        try {
            File file = new File(javaFile);
            this.filesClient.writeStringToFile(file, scriptLibrary.getDefinition(), StandardCharsets.UTF_8);
            init();
        }catch(ProcessException e){
            e.printStackTrace();
            response = new ScriptValidationResponse(false, e.getMessage());
        }
        return response;
    }

    public ScriptValidationResponse validateLibrary(String libraryName, Processor.Environment environmentType, String script, List<String> dependencies){
        ScriptValidationResponse response = new ScriptValidationResponse();
        try {
            Set<String> convertedDependencies = dependencies.stream().map(each -> convertValidatedNameToLibraryName(each)).collect(Collectors.toSet());
            logger.debug("Validating library for {} with dependencies {}", libraryName, convertedDependencies);
            GroovyScriptEngine scriptEngine = loadScriptContext(environmentType, convertedDependencies);
            GroovyShell shell = new GroovyShell(scriptEngine.getGroovyClassLoader(), scriptEngine.getConfig());
            if(logger.isDebugEnabled()){
                logger.debug("Loaded classes are {}", Arrays.stream(scriptEngine.getGroovyClassLoader().getLoadedClasses()).map(className -> className.getName()).collect(Collectors.toSet()));
            }
            Script myScript = shell.parse(script);
        }catch(Exception e){
            e.printStackTrace();
            response = new ScriptValidationResponse(false, e.getMessage());
        }
        return response;
    }

    public String convertValidatedNameToLibraryName(String libraryName){
        return libraryName.replaceAll("([\\w]+): (.*)", "$2");
    }

    public java.io.File convertLibraryNameToFile(String libraryName){
        StringBuilder pathBuilder = new StringBuilder(appConfiguration.getScriptLibraryDirectory());
        if(!appConfiguration.getScriptLibraryDirectory().endsWith("/")) pathBuilder.append("/");
        pathBuilder.append(libraryName.replaceAll("([\\w]+): (.*)", "$1")).append("/");
        new java.io.File(pathBuilder.toString()).mkdirs();
        String path =  pathBuilder.append(convertValidatedNameToLibraryName(libraryName)).append(".groovy").toString();
        java.io.File javaFile = new java.io.File(path);
        logger.debug("Returning cache path for {} as {}", libraryName, path);
        return javaFile;
    }

    public String getCachePath(Processor.Environment environmentType){
        StringBuilder pathBuilder = new StringBuilder(appConfiguration.getScriptLibraryDirectory());
        if(!appConfiguration.getScriptLibraryDirectory().endsWith("/")) pathBuilder.append("/");
        pathBuilder.append(environmentType.toLabel()).append("/");
        String path = pathBuilder.toString();
        new java.io.File(path).mkdirs();
        logger.debug("Returning cache path for {} as {}", environmentType, path);
        return path;
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
