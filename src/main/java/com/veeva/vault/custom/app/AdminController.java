package com.veeva.vault.custom.app;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.veeva.vault.custom.app.admin.*;
import com.veeva.vault.custom.app.model.files.File;
import com.veeva.vault.custom.app.repository.*;
import com.veeva.vault.custom.app.client.*;
import com.veeva.vault.custom.app.model.json.*;

import com.veeva.vault.vapil.api.client.*;
import com.veeva.vault.vapil.api.model.response.*;
import com.veeva.vault.vapil.api.request.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;

import static com.veeva.vault.custom.app.client.Client.VAULT_CLIENT_ID;

@Controller
@RequestMapping(path = "/admin/")
public class AdminController {
    @Autowired
    VaultSessionRepository sessionRepository;

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    ScriptExecutionUtils scriptExecutionUtils;

    @Autowired
    VaultConfigurationRepository configurationRepository;

    @Autowired
    ThreadRegistry threadRegistry;

    @Autowired
    ContextRepository cacheRepository;

    FilesClient filesClient;

    @Autowired
    public void setFilesClient(){
        this.filesClient = new FilesClient();
    }

    @GetMapping(path = "/customerCode", produces = "text/plain")
    public ResponseEntity customerCode(@RequestParam String customerName) {
        return ResponseEntity.ok(URLEncoder.encode(customerName, StandardCharsets.UTF_8)+"_"+java.util.UUID.nameUUIDFromBytes(customerName.getBytes()));
    }

    @PostMapping(path = "/revert/script/{id}", produces = "text/plain")
    public ResponseEntity revertSingleProject(@PathVariable String id, @RequestHeader(name = "Authorization") String sessionId) {
        threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), null));
        VaultClient vaultClient = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(appConfiguration.getVaultConfigurationHost()).withVaultClientId(VAULT_CLIENT_ID).withVaultSessionId(sessionId).build();
        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        Processor processor = configurationRepository.findById(id).orElse(null);
        ResponseEntity response = null;
        if(processor == null){
            response = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
        try {
            JsonObject jsonRecord = new JsonObject(objectMapper.writeValueAsString(processor));
            JsonArray data = new JsonArray();
            data.put(jsonRecord);
            ObjectRecordRequest objectRecordRequest = vaultClient.newRequest(ObjectRecordRequest.class);
            objectRecordRequest.setRequestString(data.toString());
            objectRecordRequest.setContentTypeJson();
            objectRecordRequest.setIdParam("id");
            ObjectRecordBulkResponse recordUpdateResponse = objectRecordRequest.updateObjectRecords(Processor.OBJECT_NAME);
            if(recordUpdateResponse.hasErrors() && recordUpdateResponse.getErrors()!=null){
                throw new Exception(recordUpdateResponse.getErrors().get(0).getMessage());
            }else{
                response = ResponseEntity.ok("SUCCESS");
            }
        }catch(Exception e){
            response = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(e.getMessage());
        }
        Logger.getLogs(Thread.currentThread().getName());
        return response;
    }

    @PostMapping(path = "/revert/library/{libraryName}", produces = "text/plain")
    public ResponseEntity revertSingleLibrary(@PathVariable String libraryName, @RequestHeader(name = "Authorization") String sessionId) throws Exception {
        threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), null));
        VaultClient vaultClient = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(appConfiguration.getVaultConfigurationHost()).withVaultClientId(VAULT_CLIENT_ID).withVaultSessionId(sessionId).build();
        java.io.File javaFile = scriptExecutionUtils.convertLibraryNameToFile(libraryName);
        if(!javaFile.exists()) return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        File file = new File(javaFile);
        String content = filesClient.readFileToString(file, StandardCharsets.UTF_8);
        ResponseEntity response = null;
        if(content == null){
            response = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
        try {
            JsonObject jsonRecord = new JsonObject();
            jsonRecord.put("name__v", libraryName);
            jsonRecord.put("definition", content);
            JsonArray data = new JsonArray();
            data.put(jsonRecord);
            ObjectRecordRequest objectRecordRequest = vaultClient.newRequest(ObjectRecordRequest.class);
            objectRecordRequest.setRequestString(data.toString());
            objectRecordRequest.setContentTypeJson();
            objectRecordRequest.setIdParam("id");
            ObjectRecordBulkResponse recordUpdateResponse = objectRecordRequest.updateObjectRecords(ScriptLibrary.OBJECT_NAME);
            if(recordUpdateResponse.hasErrors() && recordUpdateResponse.getErrors()!=null){
                throw new Exception(recordUpdateResponse.getErrors().get(0).getMessage());
            }else{
                response = ResponseEntity.ok("SUCCESS");
            }
        }catch(Exception e){
            response = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(e.getMessage());
        }
        Logger.getLogs(Thread.currentThread().getName());
        return response;
    }

    @PostMapping(path = "/deploy/script/{id}", produces = "text/plain")
    public ResponseEntity deploySingleProject(@PathVariable String id, @RequestHeader(name = "Authorization") String sessionId) {
        threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), null));
        VaultClient vaultClient = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(appConfiguration.getVaultConfigurationHost()).withVaultClientId(VAULT_CLIENT_ID).withVaultSessionId(sessionId).build();
        QueryRequest queryRequest = vaultClient.newRequest(QueryRequest.class);
        QueryResponse queryResponse = queryRequest.query("SELECT id, log_level__c, object_type__vr.api_name__v, environment_type__c, customer__cr.name__v, customer__cr.api_name__c, endpoint_url__c, authentication_method__c, LONGTEXT(definition__c), LONGTEXT(configuration__c), api_token__c, response_type__c, method__c, (SELECT script_library__cr.class_name__c, script_library__cr.package_name__c, script_library__cr.validated_name__c FROM libraryprocessor_joins__cr) FROM processor__c WHERE id = '"+id+"'");
        System.out.println(queryResponse.getResponseJSON().toString());
        String jsonResponse = queryResponse.getData().get(0).toJsonString();
        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        ResponseEntity response = null;
        try{
            Processor processor = objectMapper.readerFor(Processor.class).readValue(jsonResponse);
            configurationRepository.save(processor);
            ScriptExecutionUtils.ScriptValidationResponse validationResponse = scriptExecutionUtils.deployScript(processor);
            if(validationResponse.isValidated()){
                response = ResponseEntity.ok("SUCCESS");
            }else{
                response = ResponseEntity.badRequest().body(validationResponse.getValidationMessage());
            }
        }catch (Exception e){
            response = ResponseEntity.badRequest().body("ERROR: "+e.getMessage());
        }
        Logger.getLogs(Thread.currentThread().getName());
        return response;
    }

    @PostMapping(path = "/deploy/library/{id}", produces = "text/plain")
    public ResponseEntity deploySingleLibrary(@PathVariable String id, @RequestHeader(name = "Authorization") String sessionId) {
        threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), null));
        VaultClient vaultClient = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(appConfiguration.getVaultConfigurationHost()).withVaultClientId(VAULT_CLIENT_ID).withVaultSessionId(sessionId).build();
        QueryRequest queryRequest = vaultClient.newRequest(QueryRequest.class);
        QueryResponse queryResponse = queryRequest.query("SELECT id, package_name__c, class_name__c, validated_name__c, LONGTEXT(definition__c) FROM script_library__c where id = '"+id+"'");
        String jsonResponse = queryResponse.getData().get(0).toJsonString();
        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        ResponseEntity response = null;
        try{
            ScriptLibrary scriptLibrary = objectMapper.readerFor(ScriptLibrary.class).readValue(jsonResponse);
            ScriptExecutionUtils.ScriptValidationResponse validationResponse = scriptExecutionUtils.deployLibrary(scriptLibrary);
            if(validationResponse.isValidated()){
                response = ResponseEntity.ok("SUCCESS");
            }else{
                response = ResponseEntity.badRequest().body(validationResponse.getValidationMessage());
            }
        }catch (Exception e){
            response = ResponseEntity.badRequest().body("ERROR: "+e.getMessage());
            e.printStackTrace();
        }
        Logger.getLogs(Thread.currentThread().getName());
        return response;
    }

    @GetMapping(path = "/encrypt", produces = "text/plain")
    public ResponseEntity encrypt(@RequestParam String value) {
        return ResponseEntity.ok(new EncryptionClient().encrypt(value));
    }

    @GetMapping(path = "/postSession")
    public ModelAndView postSession() {
        return new ModelAndView("postSession");
    }


    @PostMapping(path = "/postSession", produces = "text/plain")
    public ResponseEntity<String> postSession(@RequestParam Map<String,String> requestBody) {
        threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), null));
        String authorization = requestBody.get("Session.id");
        String vaultDNS = requestBody.get("vaultDNS");
        ResponseEntity response = null;
        if(vaultDNS.equals(appConfiguration.getVaultConfigurationHost()) && authorization!=null){
            Logger.getLogger(this.getClass()).info("Authorization received");
            sessionRepository.deleteAll();
            sessionRepository.save(new Session(authorization));
            response = ResponseEntity.ok("SUCCESS");
        }else{
            Logger.getLogger(this.getClass()).error("Authorization rejected {}", requestBody);
            response = ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
        }
        Logger.getLogs(Thread.currentThread().getName());
        return response;
    }

    @GetMapping(path = "/viewPlain")
    public ModelAndView viewPlain() {
        return new ModelAndView("plainViewerGet");
    }

    @PostMapping(path = "/viewPlain")
    public ModelAndView viewPlain(@RequestParam Map<String,String> requestBody, Model model) {
        threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), null));
        String authorization = requestBody.get("authorization");
        String vaultDNS = requestBody.get("vaultDNS");
        String id = requestBody.get("id");
        String field = requestBody.get("field");
        String objectName = requestBody.get("objectName");
        ModelAndView response = null;
        if(vaultDNS.equals(appConfiguration.getVaultConfigurationHost())){
            VaultClient client = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(appConfiguration.getVaultConfigurationHost()).withVaultClientId(VAULT_CLIENT_ID).withVaultSessionId(authorization).build();
            QueryResponse queryResponse = client.newRequest(QueryRequest.class).query("SELECT LONGTEXT("+field+") FROM "+objectName+" WHERE global_id__sys = '"+id+"'");
            String definition = queryResponse.getData().get(0).getString(field);
            model.addAttribute("definition", definition);
            response = new ModelAndView("plainViewerPost");
        }else{
            response = new ModelAndView("plainViewerGet");
        }
        Logger.getLogs(Thread.currentThread().getName());
        return response;
    }

    @GetMapping(path = "/viewScript")
    public ModelAndView viewScript() {
        return new ModelAndView("javaViewerGet");
    }

    @PostMapping(path = "/viewScript")
    public ModelAndView viewScript(@RequestParam Map<String,String> requestBody, Model model) {
        threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), null));
        String authorization = requestBody.get("authorization");
        String vaultDNS = requestBody.get("vaultDNS");
        String id = requestBody.get("id");
        String field = requestBody.get("field");
        String objectName = requestBody.get("objectName");
        ModelAndView response = null;
        if(vaultDNS.equals(appConfiguration.getVaultConfigurationHost())){
            VaultClient client = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(appConfiguration.getVaultConfigurationHost()).withVaultClientId(VAULT_CLIENT_ID).withVaultSessionId(authorization).build();
            QueryResponse queryResponse = client.newRequest(QueryRequest.class).query("SELECT LONGTEXT("+field+") FROM "+objectName+" WHERE global_id__sys = '"+id+"'");
            String definition = queryResponse.getData().get(0).getString(field);
            model.addAttribute("definition", definition);
            response = new ModelAndView("javaViewerPost");
        }else{
            response = new ModelAndView("javaViewerGet");
        }
        Logger.getLogs(Thread.currentThread().getName());
        return response;
    }

    @GetMapping(path = "/editScript")
    public ModelAndView editScript() {
        return new ModelAndView("javaEditorGet");
    }

    @PostMapping(path = "/editScript")
    public ModelAndView editScript(@RequestParam Map<String,String> requestBody, Model model) {
        threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), null));
        String authorization = requestBody.get("authorization");
        String vaultDNS = requestBody.get("vaultDNS");
        String id = requestBody.get("id");
        String objectName = requestBody.get("objectName");
        ModelAndView response = null;
        String field = requestBody.get("field");
        if(vaultDNS.equals(appConfiguration.getVaultConfigurationHost())){
            VaultClient client = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(appConfiguration.getVaultConfigurationHost()).withVaultClientId(VAULT_CLIENT_ID).withVaultSessionId(authorization).build();
            QueryResponse queryResponse = client.newRequest(QueryRequest.class).query("SELECT LONGTEXT("+field+") FROM "+objectName+" WHERE id = '"+id+"'");
            String definition = queryResponse.getData().get(0).getString(field);
            model.addAttribute("definition", definition);
            model.addAttribute("field", field);
            model.addAttribute("id", id);
            model.addAttribute("objectName", objectName);
            model.addAttribute("vaultDNS", vaultDNS);
            model.addAttribute("authorization", authorization);
            response = new ModelAndView("javaEditorPost");
        }else{
            response = new ModelAndView("javaEditorGet");
        }
        Logger.getLogs(Thread.currentThread().getName());
        return response;
    }

    @RequestMapping(value="/saveScript",method=RequestMethod.POST)
    public ModelAndView saveScript(@RequestParam String vaultDNS, @RequestParam String authorization, @RequestParam String objectName, @RequestParam String id, @RequestParam String field, @RequestParam String definition, Model model) {
        threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), null));
        VaultClient client = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(appConfiguration.getVaultConfigurationHost()).withVaultClientId(VAULT_CLIENT_ID).withVaultSessionId(authorization).build();
        ScriptExecutionUtils.ScriptValidationResponse validationResponse = scriptExecutionUtils.newScriptValidationResponseInstance();
        if(field.equals("definition__c")){
            String relationshipQuery = objectName.equals("script_library__c")? "SELECT id, environment_type__c, package_name__c, class_name__c, (SELECT id, referring_library__cr.package_name__c, referring_library__cr.class_name__c, referring_library__cr.validated_name__c FROM childibrary_joins__cr where status__v = 'active__v') FROM "+objectName+" WHERE id = '"+id+"'" : "SELECT id, environment_type__c, (SELECT script_library__cr.class_name__c, script_library__cr.package_name__c, script_library__cr.validated_name__c FROM libraryprocessor_joins__cr where status__v = 'active__v') FROM "+objectName+" WHERE id = '"+id+"'";
            QueryResponse queryResponse = client.newRequest(QueryRequest.class).query(relationshipQuery);
            String environmentTypeAPi = queryResponse.getData().get(0).getListString("environment_type__c").get(0);
            Processor.Environment environmentType = Processor.Environment.valueOf(environmentTypeAPi);
            List<String> dependencies = queryResponse.getData().stream().map(row -> objectName.equals("script_library__c")?  row.getSubQuery("childibrary_joins__cr") : row.getSubQuery("libraryprocessor_joins__cr")).flatMap(subQuery -> subQuery.getData().stream()).map(each -> objectName.equals("script_library__c")? each.getString("referring_library__cr.validated_name__c") : each.getString("script_library__cr.validated_name__c")).collect(Collectors.toList());
            validationResponse = objectName.equals("script_library__c")? scriptExecutionUtils.validateLibrary(queryResponse.getData().get(0).getString("package_name__c")+"."+queryResponse.getData().get(0).getString("class_name__c"), environmentType, definition, dependencies) : scriptExecutionUtils.validateScript(environmentType, definition, dependencies);
        }
        if(validationResponse.isValidated()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("id", id);
            jsonObject.put(field, definition);
            JsonArray jsonArray = new JsonArray();
            jsonArray.put(jsonObject);
            ObjectRecordRequest objectRecordRequest = client.newRequest(ObjectRecordRequest.class);
            objectRecordRequest.setRequestString(jsonArray.toString());
            objectRecordRequest.setContentTypeJson();
            objectRecordRequest.setIdParam("id");
            ObjectRecordBulkResponse response = objectRecordRequest.updateObjectRecords(objectName);
            if(response.hasErrors() && response.getErrors()!=null){
                model.addAttribute("success", false);
                model.addAttribute("message", "Failed to save field: "+response.getErrors().get(0).getMessage());
            }else{
                model.addAttribute("success", true);
                model.addAttribute("message", "Element saved successfully");
            }
        }else{
            model.addAttribute("success", false);
            model.addAttribute("message", validationResponse.getValidationMessage().length()>512? "Failed to compile script: "+validationResponse.getValidationMessage().substring(0, 512) : "Failed to compile script: "+validationResponse.getValidationMessage());
        }
        model.addAttribute("definition", definition);
        model.addAttribute("field", field);
        model.addAttribute("id", id);
        model.addAttribute("vaultDNS", vaultDNS);
        model.addAttribute("authorization", authorization);
        model.addAttribute("objectName", objectName);
        Logger.getLogs(Thread.currentThread().getName());
        return new ModelAndView("javaEditorPost");
    }

    @GetMapping(path = "/resetConfiguration")
    public ModelAndView resetConfiguration() {
        return new ModelAndView("resetConfiguration");
    }

    @PostMapping(path = "/resetConfiguration", produces = "text/plain")
    public ResponseEntity resetConfiguration(@RequestParam Map<String,String> requestBody) {
        threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), null));
        String authorization = requestBody.get("Session.id");
        String vaultDNS = requestBody.get("vaultDNS");
        if(vaultDNS.equals(appConfiguration)){
            sessionRepository.deleteAll();
            sessionRepository.save(new Session(authorization));
        }
        VaultClient vaultClient = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(appConfiguration.getVaultConfigurationHost()).withVaultClientId(VAULT_CLIENT_ID).withVaultSessionId(authorization).build();
        QueryRequest queryRequest = vaultClient.newRequest(QueryRequest.class);
        QueryResponse queryResponse = queryRequest.query("SELECT id, log_level__c, object_type__vr.api_name__v, environment_type__c, customer__cr.name__v, customer__cr.api_name__c, endpoint_url__c, authentication_method__c, LONGTEXT(definition__c), LONGTEXT(configuration__c), api_token__c, response_type__c, method__c, (SELECT script_library__cr.class_name__c, script_library__cr.package_name__c, script_library__cr.validated_name__c FROM libraryprocessor_joins__cr) FROM processor__c WHERE state__v = 'active_state__c'");
        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        for(QueryResponse.QueryResult record : queryResponse.getData()){
            String jsonResponse = record.toJsonString();
            try{
                Processor processor = objectMapper.readerFor(Processor.class).readValue(jsonResponse);
                configurationRepository.save(processor);
                ScriptExecutionUtils.ScriptValidationResponse validationResponse = scriptExecutionUtils.deployScript(processor);
            }catch (Exception e){
                Logger.getLogger(this.getClass()).error(e.getMessage(), e);
            }
        }
        Logger.getLogs(Thread.currentThread().getName());
        return ResponseEntity.ok("SUCCESS");
    }

    @GetMapping(path = "/health", produces = "application/json")
    public ResponseEntity<String> getHealth(HttpServletResponse response){
        JsonObject object = new JsonObject();
        try{
            Runtime runtime = Runtime.getRuntime();
            object.put("freeMemory", Math.round(runtime.freeMemory()/1024));
            object.put("totalMemory", runtime.totalMemory()/1024);
            object.put("threadCount", Thread.activeCount());
            object.put("status", "OK");
            object.put("responseStatus", "SUCCESS");
            response.setStatus(HttpServletResponse.SC_OK);
        }catch(Exception e){
            object.put("responseStatus", "FAILURE");
            object.put("error", e.getMessage());
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        return ResponseEntity.ok(object.toString());
    }

    @Async
    @Scheduled(fixedRate = 300000)
    public void clearCache(){
        cacheRepository.findAll().forEach(cacheContext -> {
            if(cacheContext.getLastAccessTime().isBefore(Instant.now().minusSeconds(60 * 15))){
                cacheRepository.delete(cacheContext);
            }
        });
    }
}
