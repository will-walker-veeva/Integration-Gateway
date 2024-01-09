package com.veeva.vault.custom.app;

import com.veeva.vault.custom.app.admin.*;
import com.veeva.vault.custom.app.client.Client;
import com.veeva.vault.custom.app.client.FilesClient;
import com.veeva.vault.custom.app.client.HttpClient;
import com.veeva.vault.custom.app.client.Logger;
import com.veeva.vault.custom.app.exception.ProcessException;
import com.veeva.vault.custom.app.model.files.File;
import com.veeva.vault.custom.app.model.json.JsonObject;
import com.veeva.vault.custom.app.model.http.HttpRequest;
import com.veeva.vault.custom.app.model.http.HttpResponse;
import groovy.lang.*;
import groovy.transform.CompileDynamic;
import groovy.transform.CompileStatic;
import groovy.transform.TimedInterrupt;
import groovy.transform.TypeChecked;
import groovy.util.GroovyScriptEngine;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.codehaus.groovy.transform.sc.StaticCompileTransformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 * @hidden
 */
@Service
public class ScriptUtilities {

    public ScriptUtilities(){

    }

    @Autowired
    private Client autowiredClient;

    @Autowired
    private AppConfiguration appConfiguration;

    @Autowired
    private HttpClient httpClient;

    private FilesClient filesClient;

    private Logger logger;

    private static Map<EnvironmentType, GroovyScriptEngine> libraryGroovyShells;

    public static final List<String> ALLOWED_STAR_LIST = Arrays.asList("java.util.*", "java.time.*", "java.time.format.*",
            "java.math.*", "java.nio.charset.*", "java.time.temporal.*", "java.time.zone.*",
            "java.time.chrono.*", "java.util.regex.*", "java.util.stream.*", "java.text.*",
            "com.veeva.vault.custom.app.model.core.*", "com.veeva.vault.custom.app.model.csv.*", "com.veeva.vault.custom.app.model.files.*",
            "com.veeva.vault.custom.app.model.json.*", "com.veeva.vault.custom.app.model.query.*", "com.veeva.vault.custom.app.model.http.*",
            "com.veeva.vault.vapil.*", "com.veeva.vault.vapil.api.*", "com.veeva.vault.vapil.api.client.*",
            "com.veeva.vault.vapil.api.model.*", "com.veeva.vault.vapil.api.model.builder.*",
            "com.veeva.vault.vapil.api.model.common.*", "com.veeva.vault.vapil.api.model.metadata.*",
            "com.veeva.vault.vapil.api.model.response.*", "com.veeva.vault.vapil.api.request.*",
            "com.veeva.vault.vapil.api.connector.*");
    public static final List<String> ALLOWED_LIST = Arrays.asList("com.veeva.vault.custom.app.client.Client",
            "com.veeva.vault.custom.app.client.Emaillient", "com.veeva.vault.custom.app.client.EncryptionClient",
            "com.veeva.vault.custom.app.client.FilesClient", "com.veeva.vault.custom.app.client.HttpClient",
            "com.veeva.vault.custom.app.client.JsonClient", "com.veeva.vault.custom.app.client.QueryClient",
            "com.veeva.vault.custom.app.client.Logger", "com.veeva.vault.custom.app.client.TemplateProcessorClient",
            "com.veeva.vault.custom.app.client.XmlClient", "java.lang.AbstractStringBuilder", "java.lang.Boolean",
            "java.lang.Character", "java.lang.Comparable", "java.lang.Integer", "java.lang.Number", "java.lang.Object",
            "java.lang.String", "java.lang.StringBuilder", "java.lang.Appendable", "java.lang.Byte", "java.lang.CharSequence",
            "java.lang.Float", "java.lang.Enum", "java.lang.Double", "java.lang.Exception", "java.lang.Iterable", "java.lang.Long",
            "java.lang.Math", "java.lang.StringBuffer", "java.lang.Throwable");

    private static final String[] AUTO_STAR_LIST = {"java.util", "java.time", "java.math", "java.time.format",  "com.veeva.vault.custom.app.model.core", "com.veeva.vault.custom.app.model.csv",  "com.veeva.vault.custom.app.model.files", "com.veeva.vault.custom.app.model.query", "com.veeva.vault.custom.app.model.json",  "com.veeva.vault.custom.app.model.http", "com.veeva.vault.vapil.api.client", "com.veeva.vault.vapil.api.request"};

    private static final String[] AUTO_LIST = {"java.lang.AbstractStringBuilder", "java.lang.Boolean",
            "java.lang.Character", "java.lang.Comparable", "java.lang.Integer", "java.lang.Number", "java.lang.Object",
            "java.lang.String", "java.lang.StringBuilder", "java.lang.Appendable", "java.lang.Byte", "java.lang.CharSequence",
            "java.lang.Float", "java.lang.Enum", "java.lang.Double", "java.lang.Exception", "java.lang.Iterable", "java.lang.Long",
            "java.lang.Math", "java.lang.StringBuffer", "java.lang.Throwable"};
    @Autowired
    public void init() {
        try{
            filesClient = new FilesClient();
            logger = Logger.getLogger(this.getClass());
            Arrays.stream(EnvironmentType.values()).forEach(environmentType -> writeSecureExtensionFile(environmentType));
            init(false, null);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void writeSecureExtensionFile(EnvironmentType environmentType){
        String path = getCachePath(environmentType);
        java.io.File javaFile = new java.io.File(path+"SecureExtension.groovy");
        java.io.File oldFile = new java.io.File("src/main/resources/SecureExtension.groovy");
        try{
            filesClient.writeStringToFile(new File(javaFile), filesClient.readFileToString(new File(oldFile), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void init(boolean withExceptions, EnvironmentType limitedEnvironmentType) throws Exception{
        filesClient = new FilesClient();
        logger = Logger.getLogger(this.getClass());
        libraryGroovyShells = new HashMap<EnvironmentType, GroovyScriptEngine>();
        EnvironmentType[] environments = limitedEnvironmentType!=null? new EnvironmentType[]{limitedEnvironmentType} : EnvironmentType.values();
        for(EnvironmentType environmentType : environments){
            String path = getCachePath(environmentType);
            GroovyScriptEngine scriptEngine = null;
            try {
                scriptEngine = new GroovyScriptEngine(path);
            } catch (IOException e) {
                if(withExceptions) throw e;
                else e.printStackTrace();
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
                if(withExceptions){
                    throw new Exception("Failed to load several libraries: "+ failedFiles.stream().map(file -> file.getName()).collect(Collectors.joining(", ")));
                }
            }
            libraryGroovyShells.put(environmentType, scriptEngine);
        }
    }

    protected GroovyScriptEngine loadScriptContext(EnvironmentType environmentType){
        CompilerConfiguration config = getCompilerConfiguration(environmentType);
        GroovyScriptEngine engine = libraryGroovyShells.get(environmentType);
        engine.setConfig(config);
        return engine;
    }

    protected GroovyScriptEngine loadScriptContext(EnvironmentType environmentType, Collection<String> deployedClasses) {
        CompilerConfiguration config = getCompilerConfiguration(environmentType, deployedClasses);
        GroovyScriptEngine engine = libraryGroovyShells.get(environmentType);
        engine.setConfig(config);
        return engine;
    }

    private CompilerConfiguration getCompilerConfiguration(EnvironmentType environmentType){
        final ImportCustomizer imports = new ImportCustomizer().addStarImports(AUTO_STAR_LIST).addImports(AUTO_LIST);
        final SecureASTCustomizer secure = new SecureASTCustomizer();
        secure.setAllowedStarImports(ALLOWED_STAR_LIST);
        secure.setIndirectImportCheckEnabled(true);
        secure.setDisallowedReceivers(Arrays.asList(System.class.getName()));
        secure.setAllowedImports(ALLOWED_LIST);
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(imports, secure, getTimeoutCustomizer());
        config.setTargetDirectory(getCachePath(environmentType));
        return config;
    }

    private CompilerConfiguration getCompilerConfiguration(EnvironmentType environmentType, Collection<String> supportingClasses){
        final ImportCustomizer imports = new ImportCustomizer().addStarImports(AUTO_STAR_LIST).addImports(AUTO_LIST);
        supportingClasses.stream().forEach(supportingClass -> imports.addImports(supportingClass));
        final SecureASTCustomizer secure = new SecureASTCustomizer();
        secure.setAllowedStarImports(ALLOWED_STAR_LIST);
        secure.setIndirectImportCheckEnabled(true);
        secure.setDisallowedReceivers(Arrays.asList(System.class.getName()));
        secure.setAllowedImports(Stream.concat(ALLOWED_LIST.stream(), supportingClasses.stream()).collect(Collectors.toList()));
        CompilerConfiguration config = new CompilerConfiguration();
        config.setTargetBytecode(CompilerConfiguration.JDK17);
        config.addCompilationCustomizers(getTimeoutCustomizer(), imports, secure);
        config.setTargetDirectory(getCachePath(environmentType));
        return config;
    }

    private ASTTransformationCustomizer getTimeoutCustomizer(){
        Map<String, Object> annotationParams = new HashMap<>();
        annotationParams.put("value", 300L);
        annotationParams.put("unit", TimeUnit.SECONDS);
        ASTTransformationCustomizer transformationCustomizer = new ASTTransformationCustomizer(annotationParams, TimedInterrupt.class);
        return transformationCustomizer;
    }

    /*private ASTTransformationCustomizer getStaticCompilerCustomizer(EnvironmentType environmentType){
        Map<String, Object> annotationParams = new HashMap<>();
        annotationParams.put("extensions", "SecureExtension.groovy");
        ASTTransformationCustomizer astcz = new ASTTransformationCustomizer(annotationParams, CompileDynamic.class);
        return astcz;
    }

    private ASTTransformationCustomizer getTypeCheckedCustomizer(EnvironmentType environmentType){
        Map<String, Object> annotationParams = new HashMap<>();
        annotationParams.put("extensions", Arrays.asList("SecureExtension.groovy"));
        return new ASTTransformationCustomizer(annotationParams, TypeChecked.class);
    }*/


    public Client executeScript(HttpRequest request, Processor processor, HttpResponse response){
        Logger logger = Logger.getLogger(processor.getId());
        Set<String> scriptLibraryClasses = processor.getScriptLibraryHolder()!=null && processor.getScriptLibraryHolder().getScriptLibraries()!=null? processor.getScriptLibraryHolder().getScriptLibraries().stream().map(scriptLibrary -> scriptLibrary.getPackageName()+"."+scriptLibrary.getClassName()).collect(Collectors.toSet()) : null;
        GroovyScriptEngine scriptEngine = scriptLibraryClasses!=null? loadScriptContext(processor.getEnvironmentType(), scriptLibraryClasses) : loadScriptContext(processor.getEnvironmentType());
        try {
            Client client = null;
            if (processor.getProcessorType() == Processor.Type.base__v && processor.getAuthenticationMethod() == Processor.AuthenticationMethod.session_id__c) {
                client = new Client(autowiredClient, processor.getId(), request.getHeaders().get("x-vault-dns"), request.getHeaders().get("authorization"));
            } else if (processor.getProcessorType() == Processor.Type.spark__c) {
                client = getSparkClient(request, processor, response);
            } else {
                client = new Client(autowiredClient, processor.getId(), request.getHeaders().get("x-vault-dns"), request.getHeaders().get("authorization"));
            }
            Binding scriptContext = buildBinding(request, processor, client, response);
            try {
                scriptEngine.run(processor.getId()+".groovy", scriptContext);
            } catch(Exception e){
                logger.error(e.getMessage(), e);
                if(e instanceof TimeoutException)  response.setResponseCode(408);
                else response.setResponseCode(401);
                response.setResponseBody(httpClient.buildResponseBody(request, e));
            }
            client.setLogs(logger.getLogs(Thread.currentThread().getName()));
            client.query().dropTables();
            return client;
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.setResponseCode(401);
            response.setResponseBody(httpClient.buildResponseBody(request, e));
            Client client = new Client(autowiredClient, processor.getId());
            client.setLogs(logger.getLogs(Thread.currentThread().getName()));
            return client;
        }
    }



    private Binding buildBinding(HttpRequest request, Processor processor, Client client, HttpResponse response){
        Binding scriptContext = new Binding();
        scriptContext.setVariable("response", response);
        scriptContext.setVariable("request", request);
        scriptContext.setVariable("configuration", processor.getConfiguration());
        scriptContext.setVariable("client", client);
        scriptContext.setVariable("logger", Logger.getLogger(request.getRequestProcessorId()));
        return scriptContext;
    }

    private Client getSparkClient(HttpRequest request, Processor processor, HttpResponse response) throws Exception{
        JsonObject jsonObject = new JsonObject(request.getRequestBody());
        String vaultDNS = jsonObject.getString("vault_host_name");
        String sessionId = jsonObject.getJsonObject("message").getJsonObject("attributes").getString("authorization");
        Client client = new Client(autowiredClient, processor.getId(), vaultDNS, sessionId);
        return client;
    }

    public ScriptValidationResponse newScriptValidationResponseInstance(){
        return new ScriptValidationResponse(true);
    }

    public ScriptValidationResponse validateScript(Processor processor){
        return validateScript(processor, false);
    }
    public ScriptValidationResponse validateScript(Processor processor, boolean shouldCacheSource){
        ScriptValidationResponse response = new ScriptValidationResponse();
        try {
            Set<String> convertedDependencies = processor.getScriptLibraryHolder().getScriptLibraries().stream().map(each -> each.getValidatedName()).map(each -> convertValidatedNameToLibraryName(each)).collect(Collectors.toSet());
            logger.debug("Validating script with dependencies {}", convertedDependencies);
            GroovyScriptEngine scriptEngine = loadScriptContext(processor.getEnvironmentType(), convertedDependencies);
            scriptEngine.getGroovyClassLoader().parseClass(new GroovyCodeSource(processor.getDefinition(), processor.getId()+".groovy", ""), shouldCacheSource);
        }catch(Exception e){
            response = new ScriptValidationResponse(false, e.getMessage());
        }
        return response;
    }

    public ScriptValidationResponse deployScript(Processor processor){
        ScriptValidationResponse response = validateScript(processor, true);
        if(response.isValidated()) {
            String libraryName = processor.getId();
            java.io.File javaFile = new java.io.File(getCachePath(processor.getEnvironmentType()) + libraryName + ".groovy");
            try {
                File file = new File(javaFile);
                this.filesClient.writeStringToFile(file, processor.getDefinition(), StandardCharsets.UTF_8);
            } catch (ProcessException e) {
                e.printStackTrace();
                response = new ScriptValidationResponse(false, e.getMessage());
            }
        }
        return response;
    }

    public ScriptValidationResponse deployLibrary(ScriptLibrary scriptLibrary){
        ScriptValidationResponse response = validateLibrary(scriptLibrary);
        if(response.isValidated()) {
            String libraryName = scriptLibrary.getValidatedName();
            java.io.File javaFile = convertLibraryNameToFile(libraryName);
            try {
                File file = new File(javaFile);
                this.filesClient.writeStringToFile(file, scriptLibrary.getDefinition(), StandardCharsets.UTF_8);
                init(true, scriptLibrary.getEnvironmentType());
            } catch (Exception e) {
                e.printStackTrace();
                response = new ScriptValidationResponse(false, e.getMessage());
            }
        }
        return response;
    }

    public ScriptValidationResponse validateLibrary(ScriptLibrary scriptLibrary){
        ScriptValidationResponse response = new ScriptValidationResponse();
        try {
            Set<String> convertedDependencies = scriptLibrary.getScriptLibraryHolder().getScriptLibraries().stream().map(each -> each.getValidatedName()).map(each -> convertValidatedNameToLibraryName(each)).collect(Collectors.toSet());
            logger.debug("Validating library for {} with dependencies {}", scriptLibrary.getName(), convertedDependencies);
            GroovyScriptEngine scriptEngine = loadScriptContext(scriptLibrary.getEnvironmentType(), convertedDependencies);
            GroovyShell shell = new GroovyShell(scriptEngine.getGroovyClassLoader(), scriptEngine.getConfig());
            if(logger.isDebugEnabled()){
                logger.debug("Loaded classes are {}", Arrays.stream(scriptEngine.getGroovyClassLoader().getLoadedClasses()).map(className -> className.getName()).collect(Collectors.toSet()));
            }
            Script myScript = shell.parse(scriptLibrary.getDefinition());
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

    public String getCachePath(EnvironmentType environmentType){
        StringBuilder pathBuilder = new StringBuilder(appConfiguration.getScriptLibraryDirectory());
        if(!appConfiguration.getScriptLibraryDirectory().endsWith("/")) pathBuilder.append("/");
        pathBuilder.append(environmentType.toLabel()).append("/");
        String path = pathBuilder.toString();
        new java.io.File(path).mkdirs();
        logger.debug("Returning cache path for {} as {}", environmentType, path);
        return path;
    }

    /**
     * @hidden
     */
    public static class ScriptValidationResponse{
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
