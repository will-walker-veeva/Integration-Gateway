package com.veeva.vault.custom.app;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.veeva.vault.custom.app.admin.*;
import com.veeva.vault.custom.app.client.ScriptExecutionUtils;
import com.veeva.vault.custom.app.repository.ThreadRegistry;
import com.veeva.vault.custom.app.repository.VaultConfigurationRepository;
import com.veeva.vault.custom.app.repository.VaultSessionRepository;
import com.veeva.vault.custom.app.client.Client;
import com.veeva.vault.custom.app.client.Logger;
import com.veeva.vault.custom.app.model.http.HttpRequest;
import com.veeva.vault.custom.app.model.http.HttpResponse;
import com.veeva.vault.vapil.api.client.VaultClient;
import com.veeva.vault.vapil.api.model.response.ObjectRecordBulkResponse;
import com.veeva.vault.vapil.api.request.ObjectRecordRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.veeva.vault.custom.app.client.Client.VAULT_CLIENT_ID;

@Controller
@RequestMapping(path = "/api/")
public class RequestController {
    private static final String API_VERSION = "v23.2";
    @Autowired
    VaultConfigurationRepository configurationRepository;

    @Autowired
    VaultSessionRepository sessionRepository;

    @Autowired
    ScriptExecutionUtils scriptExecutionUtils;

    @Autowired
    AppConfiguration appConfiguration;

    @Autowired
    ThreadRegistry threadRegistry;

    ArrayBlockingQueue<SparkMessageRequest> sparkMessageQueue = new ArrayBlockingQueue<>(20000);

    @PostMapping(path = "/rest/{environment}/{customerId}/{endPoint}", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity handleFormPost(@PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, @RequestParam MultiValueMap<String,String> paramMap, HttpServletRequest request, HttpServletResponse response){
        Map.Entry<Integer, Processor> entry = validateRequest(environment, customerId, endPoint, Processor.Type.base__v, "POST", request);
        ResponseEntity responseEntity;
        if(entry!=null) {
            Processor processor = entry.getValue();
            Integer statusCode = entry.getKey();
            if (processor != null && statusCode != null && statusCode == 200){
                threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), processor.getId()));
                HttpRequest httpRequest = new HttpRequest(processor.getId(), httpHeaders.toSingleValueMap(), paramMap.toSingleValueMap(), null, request.getRemoteAddr(), request.getRemoteHost());
                try {
                    HttpResponse httpResponse = new HttpResponse();
                    Client client = scriptExecutionUtils.executeScript(httpRequest, processor, httpResponse);
                    if (client != null && httpResponse.getResponseCode() == 200) {
                        if (processor.getResponseType() == Processor.ResponseType.file__c) {
                            responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(IOUtils.toByteArray(httpResponse.getResponseFile().getInputStream()));
                        } else if (processor.getResponseType() == Processor.ResponseType.webpage__c) {
                            responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseView());
                        } else {
                            responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                        }
                        closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
                    } else if (client != null && httpResponse.getResponseCode() != 200) {
                        responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                        closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
                    } else {
                        responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                    }
                } catch (Exception e) {
                    responseEntity = ResponseEntity.internalServerError().body(e.getMessage());
                }
            } else if(processor !=null && statusCode != null && statusCode != 200){
                responseEntity = ResponseEntity.status(statusCode).build();
            } else{
                responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }
        }else{
            responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
        return responseEntity;
    }

    @PostMapping(path = "/rest/{environment}/{customerId}/{endPoint}", consumes = "application/json")
    public ResponseEntity handleFormPost(@PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, @RequestParam MultiValueMap<String,String> paramMap, @RequestBody String body, HttpServletRequest request, HttpServletResponse response){
        Map.Entry<Integer, Processor> entry = validateRequest(environment, customerId, endPoint, Processor.Type.base__v, "POST", request);
        ResponseEntity responseEntity;
        if(entry!=null) {
            Processor processor = entry.getValue();
            Integer statusCode = entry.getKey();
            if (processor != null && statusCode != null && statusCode == 200) {
                threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), processor.getId()));
                HttpRequest httpRequest = new HttpRequest(processor.getId(), httpHeaders.toSingleValueMap(), paramMap.toSingleValueMap(), body, request.getRemoteAddr(), request.getRemoteHost());
                HttpResponse httpResponse = new HttpResponse();
                Client client = scriptExecutionUtils.executeScript(httpRequest, processor, httpResponse);
                if (client != null && httpResponse.getResponseCode() == 200) {
                    if (processor.getResponseType() == Processor.ResponseType.file__c) {
                        try {
                            responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(IOUtils.toByteArray(httpResponse.getResponseFile().getInputStream()));
                        } catch (Exception e) {
                            responseEntity = ResponseEntity.internalServerError().body(e.getMessage());
                        }
                    } else if (processor.getResponseType() == Processor.ResponseType.webpage__c) {
                        responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseView());
                    } else {
                        responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                    }
                    closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
                } else if (client != null && httpResponse.getResponseCode() != 200) {
                    responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                    closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
                } else {
                    responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                }
            }else if(processor !=null && statusCode != null && statusCode != 200){
                responseEntity = ResponseEntity.status(statusCode).build();
            } else{
                responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }
        }else{
            responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
        return responseEntity;
    }

    @GetMapping(path = "/rest/{environment}/{customerId}/{endPoint}")
    public ResponseEntity handleGet(@PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, @RequestParam MultiValueMap<String,String> paramMap, HttpServletRequest request, HttpServletResponse response){
        Map.Entry<Integer, Processor> entry = validateRequest(environment, customerId, endPoint, Processor.Type.base__v, "GET", request);
        ResponseEntity responseEntity;
        if(entry!=null) {
            Processor processor = entry.getValue();
            Integer statusCode = entry.getKey();
            if (processor != null && statusCode != null && statusCode == 200) {
                threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), processor.getId()));
                HttpRequest httpRequest = new HttpRequest(processor.getId(), httpHeaders.toSingleValueMap(), paramMap.toSingleValueMap(), null, request.getRemoteAddr(), request.getRemoteHost());
                try {
                    HttpResponse httpResponse = new HttpResponse();
                    Client client = scriptExecutionUtils.executeScript(httpRequest, processor, httpResponse);
                    if (client != null && httpResponse.getResponseCode() == 200) {
                        if (processor.getResponseType() == Processor.ResponseType.file__c) {
                            responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(IOUtils.toByteArray(httpResponse.getResponseFile().getInputStream()));
                        } else if (processor.getResponseType() == Processor.ResponseType.webpage__c) {
                            responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseView());
                        } else {
                            responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                        }
                        closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
                    } else if (client != null && httpResponse.getResponseCode() != 200) {
                        responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                        closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
                    } else {
                        responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                    }
                } catch (Exception e) {
                    responseEntity = ResponseEntity.internalServerError().body(e.getMessage());
                }
            }else if(processor !=null && statusCode != null && statusCode != 200){
                responseEntity = ResponseEntity.status(statusCode).build();
            } else{
                responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }
        }else{
            responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
        return responseEntity;
    }

    @PostMapping(path = "/spark/{environment}/{customerId}/{endPoint}", produces = "application/json")
    public ResponseEntity handleSparkMessage(@PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, @RequestBody(required=true) String body, HttpServletRequest request, HttpServletResponse response){
        Map<String,String> headers = httpHeaders.toSingleValueMap();
        String certificateId = headers.get("X-VaultAPISignature-CertificateId");
        if(certificateId == null) certificateId =  headers.get("x-vaultapisignature-certificateid");
        JSONObject jsonBody = null;
        String vaultName = null;
        String sessionId = null;
        JSONObject message = null;
        try {
            jsonBody = new JSONObject(body);
            vaultName = jsonBody.optString("vault_host_name");
            message = jsonBody.optJSONObject("message");
            JSONObject attributes = message != null ? message.optJSONObject("attributes") : null;
            sessionId = attributes != null ? attributes.optString("authorization") : null;
        }catch(Exception e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }

        if(vaultName == null || sessionId == null ){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
        }

        String certificateFile = null;

        try{
            certificateFile = SparkUtilities.getPublicKey(vaultName, API_VERSION, sessionId, certificateId);
        }catch(Exception e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        }

        if(certificateFile == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ResponseEntity.status(HttpServletResponse.SC_FORBIDDEN).build();
        }

        String pemKey = certificateFile
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replaceAll("\\R", "")
                .replace("-----END CERTIFICATE-----", "");

        // Decodes key from base 64
        byte[] decoded = Base64.getMimeDecoder().decode(pemKey.getBytes());
        InputStream certStream = new ByteArrayInputStream(decoded);
        PublicKey publicKey = null;
        try {
            Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(certStream);
            publicKey = certificate.getPublicKey();
        } catch (CertificateException e) {

        }
        boolean isValidMessage = SparkUtilities.validate(headers, body, publicKey);

        // If spark message is valid, send to sqs queue for further processing
        // If not, throw error
        if(isValidMessage) {
            Map.Entry<Integer, Processor> entry = validateRequest(environment, customerId, endPoint, Processor.Type.spark__c, "POST", request);
            if(entry!=null) {
                Processor processor = entry.getValue();
                Integer statusCode = entry.getKey();
                if (processor != null && statusCode != null && statusCode == 200) {
                    this.sparkMessageQueue.add(new SparkMessageRequest(new HttpRequest(processor.getId(), httpHeaders.toSingleValueMap(), new HashMap<String, String>(), body, request.getRemoteAddr(), request.getRemoteHost()), processor));
                    return ResponseEntity.ok("SUCCESS");
                }else if(processor !=null && statusCode != null && statusCode != 200){
                    return ResponseEntity.status(statusCode).build();
                } else{
                    return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
                }
            }else{
                return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }

        } else {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
        }
    }

    @GetMapping(path = "/job/{environment}/{customerId}/{endPoint}")
    public ModelAndView getJob(@PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response, Model model) {
        Map.Entry<Integer, Processor> entry = validateRequest(environment, customerId, endPoint, Processor.Type.web_job__c, "POST", request);
        if(entry!=null) {
            Processor processor = entry.getValue();
            Integer statusCode = entry.getKey();
            if (processor != null && statusCode != null && statusCode == 200) {
                model.addAttribute("environment", environment);
                model.addAttribute("customerId", customerId);
                model.addAttribute("endpoint", endPoint);
                return new ModelAndView("jobPost");
            }
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return null;
    }

    @GetMapping(path = "/webaction/{environment}/{customerId}/{endPoint}")
    public ModelAndView getWebAction(@PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response, Model model) {
        Map.Entry<Integer, Processor> entry = validateRequest(environment, customerId, endPoint, Processor.Type.web_action__c, "POST", request);
        if(entry!=null) {
            Processor processor = entry.getValue();
            Integer statusCode = entry.getKey();
            if (processor != null && statusCode != null && statusCode == 200) {
                model.addAttribute("environment", environment);
                model.addAttribute("customerId", customerId);
                model.addAttribute("endpoint", endPoint);
                return new ModelAndView("webActionPost");
            }
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return null;
    }

    @PostMapping(path = "/job/{environment}/{customerId}/{endPoint}")
    public ResponseEntity<String> postJob(@RequestParam Map<String,String> requestBody, @PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response) {
        Map.Entry<Integer, Processor> entry = validateRequest(environment, customerId, endPoint, Processor.Type.web_job__c, "POST", request);
        ResponseEntity responseEntity;
        if(entry!=null) {
            Processor processor = entry.getValue();
            Integer statusCode = entry.getKey();
            if (processor != null && statusCode != null && statusCode == 200) {
                threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), processor.getId()));
                httpHeaders.add("authorization", requestBody.get("Session.id"));
                httpHeaders.add("x-vault-dns", requestBody.get("vaultDNS"));
                HttpRequest httpRequest = new HttpRequest(processor.getId(), httpHeaders.toSingleValueMap(), requestBody, null, request.getRemoteAddr(), request.getRemoteHost());
                try {
                    HttpResponse httpResponse = new HttpResponse();
                    Client client = scriptExecutionUtils.executeScript(httpRequest, processor, httpResponse);
                    if (client != null && httpResponse.getResponseCode() == 200) {
                        responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body("SUCCESS");
                        closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
                    } else if (client != null && httpResponse.getResponseCode() != 200) {
                        responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                        closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
                    } else {
                        responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                    }
                } catch (Exception e) {
                    responseEntity = ResponseEntity.internalServerError().body(e.getMessage());
                }
            }else if(processor !=null && statusCode != null && statusCode != 200){
                responseEntity = ResponseEntity.status(statusCode).build();
            } else{
                responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }
        }else{
            responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
        return responseEntity;
    }

    @PostMapping(path = "/webaction/{environment}/{customerId}/{endPoint}")
    public ResponseEntity<String> postWebAction(@RequestParam Map<String,String> requestBody, @PathVariable String environment, @PathVariable String customerId, @PathVariable String endPoint, @RequestHeader HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response) {
        Map.Entry<Integer, Processor> entry = validateRequest(environment, customerId, endPoint, Processor.Type.web_action__c, "POST", request);
        ResponseEntity responseEntity;
        if(entry!=null) {
            Processor processor = entry.getValue();
            Integer statusCode = entry.getKey();
            if (processor != null && statusCode != null && statusCode == 200) {
                threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), processor.getId()));
                httpHeaders.add("authorization", requestBody.get("Session.id"));
                httpHeaders.add("x-vault-dns", requestBody.get("vaultDNS"));
                HttpRequest httpRequest = new HttpRequest(processor.getId(), httpHeaders.toSingleValueMap(), requestBody, null, request.getRemoteAddr(), request.getRemoteHost());
                try {
                    HttpResponse httpResponse = new HttpResponse();
                    Client client = scriptExecutionUtils.executeScript(httpRequest, processor, httpResponse);
                    if (client != null && httpResponse.getResponseCode() == 200) {
                        responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseView());
                        closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
                    } else if (client != null && httpResponse.getResponseCode() != 200) {
                        responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                        closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
                    } else {
                        responseEntity = ResponseEntity.status(httpResponse.getResponseCode()).body(httpResponse.getResponseBody());
                    }
                } catch (Exception e) {
                    responseEntity = ResponseEntity.internalServerError().body(e.getMessage());
                }
            }else if(processor !=null && statusCode != null && statusCode != 200){
                responseEntity = ResponseEntity.status(statusCode).build();
            } else{
                responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
            }
        }else{
            responseEntity = ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }
        return responseEntity;
    }

    public Map.Entry<Integer, Processor> validateRequest(String environment, String customerId, String endpoint, Processor.Type processorType, String requestMethod, HttpServletRequest request){
        Processor exampleProcessor = new Processor(customerId, endpoint, processorType);
        String method = requestMethod.toLowerCase()+"__c";
        String environmentType = environment+"__c";
        Collection<Processor> processors = (Collection<Processor>) this.configurationRepository.findAll(Example.of(exampleProcessor));
        if(processors!=null && !processors.isEmpty()){
            Processor processor = processors.stream().filter(item -> item.getMethod() == Processor.Method.valueOf(method)).filter(item -> item.getEnvironmentType() == Processor.Environment.valueOf(environmentType)).findFirst().orElse(null);
            if(processor!=null) {
                //IP Filtering Possible HERE:

                //Authorization filtering
                String authorizationToken = request.getHeader("Authorization");
                if (processorType == Processor.Type.base__v && authorizationToken == null) {
                    return new AbstractMap.SimpleEntry(401, processor);
                } else if (processorType == Processor.Type.base__v && authorizationToken != null && processor.getAuthenticationMethod()!=null && processor.getAuthenticationMethod() == Processor.AuthenticationMethod.api_token__c) {
                    if(authorizationToken.equals(processor.getApiToken())) {
                        return new AbstractMap.SimpleEntry(200, processor);
                    }else{
                        return new AbstractMap.SimpleEntry(401, processor);
                    }
                }else{
                    return new AbstractMap.SimpleEntry(200, processor);
                }
            }else{
                return null;
            }
        }
        return null;
    }

    public void closeClient(String processorId, Client client, com.veeva.vault.custom.app.client.Logger.Level levelToLog){
        List<Log> logs = client.getLogs();
        if(logs!=null && !logs.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper(new JsonFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                objectMapper.registerModule(new JavaTimeModule());
                if(sessionRepository.count()>0) {
                    List<Log> publishableLogs = logs.stream().filter(each -> filter(each, levelToLog)).map(each -> {
                        each.setProcessorId(processorId);
                        return each;
                    }).sorted(Comparator.comparing(Log::getCreated).reversed()).collect(Collectors.toList());
                    Session session = sessionRepository.findAll().iterator().next();
                    VaultClient vaultClient = VaultClient.newClientBuilder(VaultClient.AuthenticationType.SESSION_ID).withVaultDNS(appConfiguration.getVaultConfigurationHost()).withVaultClientId(VAULT_CLIENT_ID).withVaultSessionId(session.getSessionId()).build();
                    ObjectRecordRequest logCreationRequest = vaultClient.newRequest(ObjectRecordRequest.class);
                    logCreationRequest.setContentTypeJson();
                    logCreationRequest.setRequestString(objectMapper.writeValueAsString(publishableLogs));
                    ObjectRecordBulkResponse response = logCreationRequest.createObjectRecords("log__c");
                }else{
                    System.out.println("NO SESSIONS");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        client.deleteAllFiles();
    }

    private boolean filter(Log log, Logger.Level level){
        switch(level){
            case INFO:
                return log.getLevel()!= Logger.Level.DEBUG && log.getLevel()!=Logger.Level.TRACE;
            case WARN:
                return log.getLevel()==Logger.Level.WARN;
            case DEBUG:
                return log.getLevel()!=Logger.Level.TRACE;
            case ERROR:
                return log.getLevel()== Logger.Level.ERROR || log.getLevel()==Logger.Level.WARN;
            case TRACE:
                return true;
        }
        return true;
    }


    @Scheduled(fixedRateString = "100")
    public void pollMessageQueue() {
        final org.apache.logging.log4j.Logger logger = LogManager.getLogger();
        SparkMessageRequest sparkRequest = null;
        try {
            sparkRequest = sparkMessageQueue.poll(50, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (sparkRequest != null) {
            Processor processor = sparkRequest.getProcessor();
            HttpRequest httpRequest = sparkRequest.getHttpRequest();
            threadRegistry.save(new ThreadItem(Thread.currentThread().getName(), processor.getId()));
            Client client = null;
            try {
                HttpResponse httpResponse = new HttpResponse();
                client = scriptExecutionUtils.executeScript(httpRequest, processor, httpResponse);

            } catch (Exception e) {
                Logger.getLogger(processor.getId()).error(e.getMessage(), e);
            }finally{
                if (client != null) {
                    closeClient(processor.getId(), client, Logger.Level.ofAPIName(processor.getLogLevel().toString()));
                }
            }
        }
    }
}
